package com.shopizer.checkout.service;

import com.shopizer.checkout.client.CartServiceClient;
import com.shopizer.checkout.client.InventoryServiceClient;
import com.shopizer.checkout.client.OrderServiceClient;
import com.shopizer.checkout.client.PaymentServiceClient;
import com.shopizer.checkout.client.PricingServiceClient;
import com.shopizer.checkout.client.PromotionsServiceClient;
import com.shopizer.checkout.client.ShippingServiceClient;
import com.shopizer.checkout.client.TaxServiceClient;
import com.shopizer.checkout.client.dto.ApplyCouponRequest;
import com.shopizer.checkout.client.dto.ApplyCouponResponse;
import com.shopizer.checkout.client.dto.AuthorizePaymentRequest;
import com.shopizer.checkout.client.dto.AuthorizePaymentResponse;
import com.shopizer.checkout.client.dto.CartResponse;
import com.shopizer.checkout.client.dto.CreateOrderItemRequest;
import com.shopizer.checkout.client.dto.CreateOrderRequest;
import com.shopizer.checkout.client.dto.InventoryReserveRequest;
import com.shopizer.checkout.client.dto.OrderResponse;
import com.shopizer.checkout.client.dto.PricingResolutionResponse;
import com.shopizer.checkout.client.dto.ShippingQuoteRequest;
import com.shopizer.checkout.client.dto.ShippingQuoteResponse;
import com.shopizer.checkout.client.dto.TaxCalculateRequest;
import com.shopizer.checkout.client.dto.TaxCalculateResponse;
import com.shopizer.checkout.client.dto.TaxLine;
import com.shopizer.checkout.service.CheckoutExceptions.CheckoutInventoryReservationException;
import com.shopizer.checkout.service.CheckoutExceptions.CheckoutOrchestrationException;
import com.shopizer.checkout.service.CheckoutExceptions.CheckoutShippingQuoteException;
import com.shopizer.checkout.service.CheckoutExceptions.CheckoutShippingSelectionException;
import com.shopizer.checkout.service.CheckoutExceptions.CheckoutValidationException;
import com.shopizer.checkout.web.dto.CreateCheckoutRequest;
import java.math.BigDecimal;
import java.util.Comparator;
import java.util.List;
import java.util.Objects;
import java.util.UUID;
import java.util.stream.Collectors;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Service;

/**
 * Flow: CreateOrderFromCartFlow
 *
 * Single canonical orchestration flow for checkout.
 *
 * Inputs:
 * - cartId, merchantStoreId, customerId
 * - storeCode (pricing)
 * - couponCode (optional)
 * - shipping destination + optional selected quote
 * - bearerToken (Authorization header value) propagated to downstream services
 *
 * Output:
 * - CheckoutResult (order, totals, payment authorization, shipping selection)
 *
 * Errors / failure modes:
 * 1) Cart not found / unauthorized => CheckoutOrchestrationException (wrapped client error)
 * 2) Cart mismatch or empty cart => CheckoutValidationException (400 at API boundary)
 * 3) Inventory reservation fails => CheckoutInventoryReservationException (409 at API boundary)
 * 4) Shipping quote downstream failure => CheckoutShippingQuoteException (502 at API boundary)
 * 5) Shipping selection not found in quotes => CheckoutShippingSelectionException (400 at API boundary)
 * 6) Order/payment failures => CheckoutOrchestrationException (502 at API boundary)
 *
 * Side effects:
 * - Network calls to cart-service, inventory-service, pricing-service, promotions-service, tax-service,
 *   shipping-service, order-service, payment-service.
 */
@Service
public class CreateOrderFromCartFlow {

  private static final Logger log = LoggerFactory.getLogger(CreateOrderFromCartFlow.class);

  private final CartServiceClient cartServiceClient;
  private final OrderServiceClient orderServiceClient;
  private final PricingServiceClient pricingServiceClient;
  private final TaxServiceClient taxServiceClient;
  private final PromotionsServiceClient promotionsServiceClient;
  private final InventoryServiceClient inventoryServiceClient;
  private final PaymentServiceClient paymentServiceClient;
  private final ShippingServiceClient shippingServiceClient;

  public CreateOrderFromCartFlow(
      CartServiceClient cartServiceClient,
      OrderServiceClient orderServiceClient,
      PricingServiceClient pricingServiceClient,
      TaxServiceClient taxServiceClient,
      PromotionsServiceClient promotionsServiceClient,
      InventoryServiceClient inventoryServiceClient,
      PaymentServiceClient paymentServiceClient,
      ShippingServiceClient shippingServiceClient
  ) {
    this.cartServiceClient = cartServiceClient;
    this.orderServiceClient = orderServiceClient;
    this.pricingServiceClient = pricingServiceClient;
    this.taxServiceClient = taxServiceClient;
    this.promotionsServiceClient = promotionsServiceClient;
    this.inventoryServiceClient = inventoryServiceClient;
    this.paymentServiceClient = paymentServiceClient;
    this.shippingServiceClient = shippingServiceClient;
  }

  /**
   * Selected shipping details included in checkout result/response.
   *
   * <p>Invariants:
   * - If requestId is null, all other fields should be null as well.
   * - If provider is non-null, serviceLevel is non-null (identity tuple).
   */
  public record ShippingSelection(
      UUID requestId,
      String provider,
      String serviceLevel,
      String serviceName,
      BigDecimal amount
  ) {}

  /**
   * Result object for checkout calculation + order creation + payment authorization (+ shipping selection).
   *
   * <p>Invariants:
   * - subtotal/discount/tax/total are non-null (BigDecimal.ZERO when not applicable).
   * - payment authorization fields are expected non-null on success.
   * - shipping selection may be null if quoting is not attempted; in this flow it is always attempted.
   */
  public record CheckoutResult(
      UUID orderId,
      BigDecimal subtotal,
      BigDecimal discount,
      BigDecimal tax,
      BigDecimal total,
      UUID paymentIntentId,
      String paymentProvider,
      String paymentStatus,
      String providerAuthorizationId,
      String providerOrderId,
      ShippingSelection shippingSelection
  ) {}

  // PUBLIC_INTERFACE
  public CheckoutResult execute(
      UUID cartId,
      UUID merchantStoreId,
      UUID customerId,
      String storeCode,
      String couponCode,
      String paymentMethod,
      CreateCheckoutRequest.Destination destination,
      CreateCheckoutRequest.SelectedShippingQuote selectedShippingQuote,
      Integer defaultItemWeightGrams,
      String bearerToken
  ) {
    /** Execute the checkout orchestration to calculate totals, quote/select shipping, create order, and authorize payment. */
    log.info(
        "flow=CreateOrderFromCartFlow event=start cartId={} merchantStoreId={} customerId={} storeCode={} hasCoupon={} paymentMethod={} destinationCountry={} selectedShippingProvided={}",
        cartId,
        merchantStoreId,
        customerId,
        storeCode,
        couponCode != null && !couponCode.isBlank(),
        paymentMethod,
        destination != null ? destination.country() : null,
        selectedShippingQuote != null
    );

    try {
      CartResponse cart = cartServiceClient.getCart(cartId, bearerToken);
      if (cart == null) {
        throw new CheckoutOrchestrationException("cart-service returned null cart", null);
      }

      validateCartMatchesRequest(cart, cartId, merchantStoreId, customerId);

      if (cart.items() == null || cart.items().isEmpty()) {
        throw new CheckoutValidationException("Cart is empty; cannot checkout");
      }

      // Phase 2: reserve inventory for all cart lines before creating an order.
      reserveInventoryOrThrow(cart, merchantStoreId, bearerToken);

      String effectiveStoreCode = normalizeStoreCode(storeCode);

      BigDecimal subtotal = calculateSubtotal(cart, effectiveStoreCode, bearerToken);
      BigDecimal discount = BigDecimal.ZERO;

      if (couponCode != null && !couponCode.isBlank()) {
        ApplyCouponResponse promo = promotionsServiceClient.applyCoupon(
            new ApplyCouponRequest(couponCode.trim(), cart.currency(), subtotal, BigDecimal.ZERO, BigDecimal.ZERO),
            bearerToken
        );
        if (promo != null && promo.discountSubtotal() != null) {
          discount = promo.discountSubtotal();
        }
      }

      // Guard against downstream returning a discount larger than subtotal.
      if (discount.compareTo(subtotal) > 0) {
        log.warn("flow=CreateOrderFromCartFlow event=discount_clamped cartId={} subtotal={} discount={}",
            cartId, subtotal, discount);
        discount = subtotal;
      }

      BigDecimal discountedSubtotal = subtotal.subtract(discount);

      TaxCalculateResponse tax = taxServiceClient.calculateTax(
          new TaxCalculateRequest(
              // See note in original implementation: tax-service expects numeric store id.
              stableStoreIdLong(merchantStoreId),
              cart.items().stream()
                  .map(i -> new TaxLine(
                      i.sku(),
                      i.quantity(),
                      resolveUnitPriceOrZero(effectiveStoreCode, i.sku(), cart.currency(), i.quantity(), bearerToken)
                  ))
                  .toList()
          ),
          bearerToken
      );

      BigDecimal taxAmount = (tax != null && tax.taxAmount() != null) ? tax.taxAmount() : BigDecimal.ZERO;

      // Phase 4: quote/select shipping.
      ShippingSelection shippingSelection = quoteAndSelectShippingOrThrow(
          cart,
          destination,
          selectedShippingQuote,
          defaultItemWeightGrams,
          bearerToken
      );

      // IMPORTANT: total currently excludes shipping amount because order/payment APIs are Phase 1.
      // We still return shipping selection so caller can display/confirm.
      BigDecimal total = discountedSubtotal.add(taxAmount);

      CreateOrderRequest createOrderRequest = mapCartToCreateOrder(cart);
      OrderResponse created = orderServiceClient.createOrder(createOrderRequest, bearerToken);

      if (created == null || created.getId() == null) {
        throw new CheckoutOrchestrationException("order-service returned null/invalid order response", null);
      }

      // Phase 1: Payment authorization currently always uses PayPal.
      // We accept `paymentMethod` for forward-compatibility with UI selection, but ignore/override it for now.
      if (paymentMethod != null && !paymentMethod.isBlank()
          && !"paypal".equalsIgnoreCase(paymentMethod.trim())
          && !"card".equalsIgnoreCase(paymentMethod.trim())
          && !"cod".equalsIgnoreCase(paymentMethod.trim())) {
        log.warn("flow=CreateOrderFromCartFlow event=payment_method_unknown cartId={} paymentMethod={}", cartId, paymentMethod);
      } else if (paymentMethod != null && !paymentMethod.isBlank() && !"paypal".equalsIgnoreCase(paymentMethod.trim())) {
        log.info("flow=CreateOrderFromCartFlow event=payment_method_ignored_phase1 cartId={} paymentMethod={}", cartId, paymentMethod);
      }

      AuthorizePaymentResponse paymentAuth = authorizePaymentOrThrow(
          merchantStoreId,
          created.getId(),
          customerId,
          cart.currency(),
          total,
          cartId,
          bearerToken
      );

      log.info(
          "flow=CreateOrderFromCartFlow event=success cartId={} orderId={} subtotal={} discount={} tax={} total={} shippingProvider={} shippingServiceLevel={} shippingAmount={} paymentStatus={} paymentIntentId={}",
          cartId,
          created.getId(),
          subtotal,
          discount,
          taxAmount,
          total,
          shippingSelection != null ? shippingSelection.provider() : null,
          shippingSelection != null ? shippingSelection.serviceLevel() : null,
          shippingSelection != null ? shippingSelection.amount() : null,
          paymentAuth != null ? paymentAuth.status() : null,
          paymentAuth != null ? paymentAuth.paymentIntentId() : null
      );

      return new CheckoutResult(
          created.getId(),
          subtotal,
          discount,
          taxAmount,
          total,
          paymentAuth.paymentIntentId(),
          paymentAuth.provider(),
          paymentAuth.status(),
          paymentAuth.providerAuthorizationId(),
          paymentAuth.providerOrderId(),
          shippingSelection
      );
    } catch (CheckoutValidationException ex) {
      log.warn("flow=CreateOrderFromCartFlow event=validation_failed cartId={} reason={}", cartId, ex.getMessage());
      throw ex;
    } catch (CheckoutShippingSelectionException ex) {
      log.warn("flow=CreateOrderFromCartFlow event=shipping_selection_invalid cartId={} reason={}", cartId, ex.getMessage());
      throw ex;
    } catch (CheckoutShippingQuoteException ex) {
      log.warn("flow=CreateOrderFromCartFlow event=shipping_quote_failed cartId={} reason={}", cartId, ex.getMessage());
      throw ex;
    } catch (Exception ex) {
      log.error("flow=CreateOrderFromCartFlow event=failed cartId={} message={}", cartId, ex.getMessage(), ex);
      if (ex instanceof CheckoutOrchestrationException) {
        throw ex;
      }
      throw new CheckoutOrchestrationException("Checkout orchestration failed", ex);
    }
  }

  private ShippingSelection quoteAndSelectShippingOrThrow(
      CartResponse cart,
      CreateCheckoutRequest.Destination destination,
      CreateCheckoutRequest.SelectedShippingQuote selectedShippingQuote,
      Integer defaultItemWeightGrams,
      String bearerToken
  ) {
    // Contract:
    // - Calls shipping-service, propagating caller JWT.
    // - If selection is provided, validates it is present.
    // - Otherwise selects the cheapest quote.
    //
    // Failure modes:
    // - shipping-service unavailable / returns error => CheckoutShippingQuoteException
    // - no quotes returned => CheckoutShippingSelectionException (caller must adjust destination/items)
    // - selection not found => CheckoutShippingSelectionException

    int fallbackWeight = (defaultItemWeightGrams != null && defaultItemWeightGrams >= 1) ? defaultItemWeightGrams : 500;

    ShippingQuoteRequest request = new ShippingQuoteRequest(
        new ShippingQuoteRequest.Destination(
            destination.country(),
            destination.postalCode(),
            destination.region()
        ),
        cart.currency(),
        cart.items().stream()
            .map(i -> new ShippingQuoteRequest.Item(i.sku(), i.quantity(), fallbackWeight))
            .toList()
    );

    ShippingQuoteResponse response;
    try {
      response = shippingServiceClient.getShippingQuotes(request, bearerToken);
    } catch (ShippingServiceClient.ShippingServiceClientException ex) {
      throw new CheckoutShippingQuoteException("shipping-service quote request failed", ex);
    }

    if (response == null || response.requestId() == null) {
      throw new CheckoutShippingQuoteException("shipping-service returned null/invalid quote response", null);
    }
    if (response.quotes() == null || response.quotes().isEmpty()) {
      throw new CheckoutShippingSelectionException(
          "No shipping quotes available for the provided destination/items (requestId=" + response.requestId() + ")"
      );
    }

    ShippingQuoteResponse.Quote selected = selectQuoteOrThrow(response, selectedShippingQuote);

    return new ShippingSelection(
        response.requestId(),
        selected.provider(),
        selected.serviceLevel(),
        selected.serviceName(),
        selected.amount()
    );
  }

  private static ShippingQuoteResponse.Quote selectQuoteOrThrow(
      ShippingQuoteResponse response,
      CreateCheckoutRequest.SelectedShippingQuote selection
  ) {
    if (selection != null) {
      return response.quotes().stream()
          .filter(q -> Objects.equals(selection.provider(), q.provider())
              && Objects.equals(selection.serviceLevel(), q.serviceLevel()))
          .findFirst()
          .orElseThrow(() -> new CheckoutShippingSelectionException(
              "Selected shipping quote not found in returned quotes (requestId=" + response.requestId()
                  + ", provider=" + selection.provider()
                  + ", serviceLevel=" + selection.serviceLevel() + ")"
          ));
    }

    // Default selection strategy: cheapest by amount (null amounts sorted last).
    return response.quotes().stream()
        .min(Comparator.comparing(
            ShippingQuoteResponse.Quote::amount,
            Comparator.nullsLast(Comparator.naturalOrder())
        ))
        .orElseThrow(() -> new CheckoutShippingSelectionException(
            "No selectable shipping quote found (requestId=" + response.requestId() + ")"
        ));
  }

  private static void validateCartMatchesRequest(CartResponse cart, UUID cartId, UUID merchantStoreId, UUID customerId) {
    // Invariant: prevent checkout of a cart not belonging to given store/customer (Phase 1 safety check)
    if (!cartId.equals(cart.id())) {
      throw new CheckoutValidationException("Cart ID mismatch");
    }
    if (!merchantStoreId.equals(cart.merchantStoreId())) {
      throw new CheckoutValidationException("merchantStoreId does not match cart");
    }
    if (!customerId.equals(cart.customerId())) {
      throw new CheckoutValidationException("customerId does not match cart");
    }
    if (cart.currency() == null || cart.currency().isBlank()) {
      throw new CheckoutValidationException("Cart currency is missing");
    }
  }

  private void reserveInventoryOrThrow(CartResponse cart, UUID merchantStoreId, String bearerToken) {
    // inventory-service expects a numeric storeId; use same stable adapter approach as tax-service.
    long storeId = stableStoreIdLong(merchantStoreId);

    cart.items().forEach(i -> {
      try {
        inventoryServiceClient.reserve(
            new InventoryReserveRequest(storeId, i.sku(), i.quantity()),
            bearerToken
        );
      } catch (InventoryServiceClient.InventoryReserveFailedException ex) {
        throw new CheckoutInventoryReservationException(
            "Inventory reservation failed for sku=" + i.sku() + " quantity=" + i.quantity(), ex);
      }
    });
  }

  private static CreateOrderRequest mapCartToCreateOrder(CartResponse cart) {
    CreateOrderRequest req = new CreateOrderRequest();
    req.setMerchantStoreId(cart.merchantStoreId());
    req.setCustomerId(cart.customerId());
    req.setCurrency(cart.currency());

    List<CreateOrderItemRequest> items = cart.items().stream()
        .map(i -> {
          CreateOrderItemRequest item = new CreateOrderItemRequest();
          item.setProductId(i.productId());
          item.setQuantity(i.quantity());
          // Phase 1: order-service model uses integer minor units; pricing integration is not yet applied to persisted order items.
          item.setUnitAmount(0L);
          return item;
        })
        .collect(Collectors.toList());

    req.setItems(items);
    return req;
  }

  private BigDecimal calculateSubtotal(CartResponse cart, String storeCode, String bearerToken) {
    // Pricing-service is resolved per SKU+qty; subtotal is sum of extended prices.
    return cart.items().stream()
        .map(i -> {
          PricingResolutionResponse resolved = pricingServiceClient.resolvePrice(
              storeCode,
              i.sku(),
              cart.currency(),
              i.quantity(),
              bearerToken
          );
          if (resolved == null || resolved.extendedPrice() == null) {
            throw new CheckoutOrchestrationException(
                "pricing-service returned null/invalid pricing response for sku=" + i.sku(), null);
          }
          return resolved.extendedPrice();
        })
        .reduce(BigDecimal.ZERO, BigDecimal::add);
  }

  private BigDecimal resolveUnitPriceOrZero(
      String storeCode,
      String sku,
      String currency,
      int qty,
      String bearerToken
  ) {
    // Tax API requires per-line unitPrice. We resolve it from pricing-service.
    PricingResolutionResponse resolved = pricingServiceClient.resolvePrice(storeCode, sku, currency, qty, bearerToken);
    if (resolved == null || resolved.unitPrice() == null) {
      log.warn("flow=CreateOrderFromCartFlow event=unit_price_missing sku={} storeCode={}", sku, storeCode);
      return BigDecimal.ZERO;
    }
    return resolved.unitPrice();
  }

  private static String normalizeStoreCode(String storeCode) {
    if (storeCode == null) {
      return "DEFAULT";
    }
    String trimmed = storeCode.trim();
    if (trimmed.isBlank()) {
      return "DEFAULT";
    }
    return trimmed;
  }

  private static long stableStoreIdLong(UUID merchantStoreId) {
    // Minimal deterministic mapping: create a positive long from UUID hash.
    // This is an interim adapter until tax-service aligns on store UUID or storeCode.
    int h = merchantStoreId.toString().hashCode();
    return Integer.toUnsignedLong(h);
  }

  private AuthorizePaymentResponse authorizePaymentOrThrow(
      UUID merchantStoreId,
      UUID orderId,
      UUID customerId,
      String currency,
      BigDecimal total,
      UUID cartId,
      String bearerToken
  ) {
    // Invariant: total is computed and non-null (see CheckoutResult contract). Treat null as 0 for safety.
    BigDecimal safeTotal = total != null ? total : BigDecimal.ZERO;
    long amountMinor = toMinorUnitsOrThrow(safeTotal);

    String idempotencyKey = buildPaymentIdempotencyKey(cartId, orderId);

    try {
      AuthorizePaymentResponse resp = paymentServiceClient.authorizePayPal(
          new AuthorizePaymentRequest(
              merchantStoreId,
              orderId,
              customerId,
              currency,
              amountMinor,
              idempotencyKey
          ),
          bearerToken
      );

      if (resp == null || resp.paymentIntentId() == null || resp.status() == null) {
        throw new CheckoutOrchestrationException("payment-service returned null/invalid authorization response", null);
      }

      return resp;
    } catch (PaymentServiceClient.PaymentServiceClientException ex) {
      // Add actionable context but preserve root cause.
      throw new CheckoutOrchestrationException(
          "Payment authorization failed (orderId=" + orderId + ", idempotencyKey=" + idempotencyKey + ")", ex);
    }
  }

  private static String buildPaymentIdempotencyKey(UUID cartId, UUID orderId) {
    // Deterministic idempotency key so client retries do not double-authorize.
    // Choice: stable across retries of the same checkout attempt (cart+order).
    return "checkout-" + cartId + "-" + orderId;
  }

  private static long toMinorUnitsOrThrow(BigDecimal amountMajor) {
    // Phase 1 contract: payment-service expects minor units (long).
    // Invariant: total has 2 decimal places in typical currencies; enforce exact conversion to avoid silent rounding bugs.
    try {
      return amountMajor.movePointRight(2).longValueExact();
    } catch (ArithmeticException ex) {
      throw new CheckoutOrchestrationException(
          "Cannot convert total amount to minor units without rounding: amount=" + amountMajor, ex);
    }
  }
}
