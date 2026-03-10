package com.shopizer.checkout.service;

import com.shopizer.checkout.client.CartServiceClient;
import com.shopizer.checkout.client.OrderServiceClient;
import com.shopizer.checkout.client.dto.CartResponse;
import com.shopizer.checkout.client.dto.CreateOrderItemRequest;
import com.shopizer.checkout.client.dto.CreateOrderRequest;
import com.shopizer.checkout.client.dto.OrderResponse;
import com.shopizer.checkout.service.CheckoutExceptions.CheckoutOrchestrationException;
import com.shopizer.checkout.service.CheckoutExceptions.CheckoutValidationException;
import java.util.List;
import java.util.UUID;
import java.util.stream.Collectors;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Service;

/**
 * Flow: CreateOrderFromCartFlow
 *
 * Single canonical orchestration flow for Phase 1 checkout.
 *
 * Inputs:
 * - cartId, merchantStoreId, customerId
 * - bearerToken (Authorization header value) propagated to downstream services
 *
 * Output:
 * - created order id
 *
 * Errors / failure modes:
 * 1) Cart not found / unauthorized => CheckoutOrchestrationException (wrapped client error)
 * 2) Cart mismatch or empty cart => CheckoutValidationException (400 at API boundary)
 * 3) Order creation fails => CheckoutOrchestrationException
 *
 * Side effects:
 * - Network calls to cart-service and order-service.
 */
@Service
public class CreateOrderFromCartFlow {

  private static final Logger log = LoggerFactory.getLogger(CreateOrderFromCartFlow.class);

  private final CartServiceClient cartServiceClient;
  private final OrderServiceClient orderServiceClient;

  public CreateOrderFromCartFlow(CartServiceClient cartServiceClient, OrderServiceClient orderServiceClient) {
    this.cartServiceClient = cartServiceClient;
    this.orderServiceClient = orderServiceClient;
  }

  // PUBLIC_INTERFACE
  public UUID execute(UUID cartId, UUID merchantStoreId, UUID customerId, String bearerToken) {
    /** Execute the checkout orchestration to create an order from the given cart. */
    log.info("flow=CreateOrderFromCartFlow event=start cartId={} merchantStoreId={} customerId={}",
        cartId, merchantStoreId, customerId);

    try {
      CartResponse cart = cartServiceClient.getCart(cartId, bearerToken);
      if (cart == null) {
        throw new CheckoutOrchestrationException("cart-service returned null cart", null);
      }

      validateCartMatchesRequest(cart, cartId, merchantStoreId, customerId);

      if (cart.items() == null || cart.items().isEmpty()) {
        throw new CheckoutValidationException("Cart is empty; cannot checkout");
      }

      CreateOrderRequest createOrderRequest = mapCartToCreateOrder(cart);
      OrderResponse created = orderServiceClient.createOrder(createOrderRequest, bearerToken);

      if (created == null || created.getId() == null) {
        throw new CheckoutOrchestrationException("order-service returned null/invalid order response", null);
      }

      log.info("flow=CreateOrderFromCartFlow event=success cartId={} orderId={}", cartId, created.getId());
      return created.getId();
    } catch (CheckoutValidationException ex) {
      log.warn("flow=CreateOrderFromCartFlow event=validation_failed cartId={} reason={}", cartId, ex.getMessage());
      throw ex;
    } catch (Exception ex) {
      log.error("flow=CreateOrderFromCartFlow event=failed cartId={} message={}", cartId, ex.getMessage(), ex);
      if (ex instanceof CheckoutOrchestrationException) {
        throw ex;
      }
      throw new CheckoutOrchestrationException("Checkout orchestration failed", ex);
    }
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
          // Phase 1: no catalog pricing integration yet.
          item.setUnitAmount(0L);
          return item;
        })
        .collect(Collectors.toList());

    req.setItems(items);
    return req;
  }
}
