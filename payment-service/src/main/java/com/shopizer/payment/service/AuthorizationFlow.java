package com.shopizer.payment.service;

import com.shopizer.payment.domain.*;
import com.shopizer.payment.repo.PaymentAuthorizationRepository;
import com.shopizer.payment.repo.PaymentIntentRepository;
import com.shopizer.payment.service.model.AuthorizationAttempt;
import com.shopizer.payment.service.model.AuthorizationResult;
import com.shopizer.payment.service.provider.PaymentAuthorizationProvider;
import com.shopizer.payment.service.provider.PaymentProviderRegistry;
import java.util.Optional;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.util.StringUtils;

/**
 * AuthorizationFlow is the canonical authorize-only flow for payment-service (Phase 1).
 *
 * Contract:
 * Inputs:
 * - attempt.merchantStoreId, attempt.orderId, attempt.currency, attempt.idempotencyKey must be present
 * - attempt.amountMinor >= 0
 *
 * Outputs:
 * - Returns persisted PaymentIntent + PaymentAuthorization state.
 *
 * Errors:
 * - ValidationException: input invalid
 * - ProviderException: provider call failed
 * - ConfigurationException: missing provider configuration
 *
 * Side effects:
 * - Writes/updates payment_intent and payment_authorization records.
 * - Calls external PayPal APIs.
 */
@Service
public class AuthorizationFlow {

  private static final Logger log = LoggerFactory.getLogger(AuthorizationFlow.class);

  private final PaymentIntentRepository intentRepo;
  private final PaymentAuthorizationRepository authorizationRepo;
  private final PaymentProviderRegistry providerRegistry;

  public AuthorizationFlow(
      PaymentIntentRepository intentRepo,
      PaymentAuthorizationRepository authorizationRepo,
      PaymentProviderRegistry providerRegistry
  ) {
    this.intentRepo = intentRepo;
    this.authorizationRepo = authorizationRepo;
    this.providerRegistry = providerRegistry;
  }

  // PUBLIC_INTERFACE
  @Transactional
  public AuthorizationOutcome authorizePayPal(AuthorizationAttempt attempt) {
    /** Authorizes a payment via PayPal with idempotency and persistence. */
    validate(attempt);

    String flowName = "AuthorizePaymentFlow";
    log.info("payment.flow.start flow={} provider={} storeId={} orderId={} idempotencyKey={}",
        flowName, PaymentProvider.PAYPAL, attempt.merchantStoreId(), attempt.orderId(), attempt.idempotencyKey());

    // Idempotency: if a PaymentIntent already exists for same (storeId, idempotencyKey), return existing record.
    Optional<PaymentIntent> existing = intentRepo.findByMerchantStoreIdAndIdempotencyKey(
        attempt.merchantStoreId(), attempt.idempotencyKey());

    if (existing.isPresent()) {
      PaymentIntent intent = existing.get();
      Optional<PaymentAuthorization> auth = authorizationRepo.findByPaymentIntent(intent);

      log.info("payment.flow.idempotent flow={} intentId={} status={}", flowName, intent.getId(), intent.getStatus());
      return AuthorizationOutcome.fromExisting(intent, auth.orElse(null));
    }

    // Create intent
    PaymentIntent intent = new PaymentIntent();
    intent.setMerchantStoreId(attempt.merchantStoreId());
    intent.setOrderId(attempt.orderId());
    intent.setCustomerId(attempt.customerId());
    intent.setCurrency(attempt.currency());
    intent.setAmountMinor(attempt.amountMinor());
    intent.setProvider(PaymentProvider.PAYPAL);
    intent.setStatus(PaymentIntentStatus.CREATED);
    intent.setIdempotencyKey(attempt.idempotencyKey());
    intent = intentRepo.save(intent);

    try {
      PaymentAuthorizationProvider provider = providerRegistry.require(PaymentProvider.PAYPAL);
      AuthorizationResult result = provider.authorize(attempt);

      // Persist authorization
      PaymentAuthorization auth = new PaymentAuthorization();
      auth.setPaymentIntent(intent);
      auth.setProvider(PaymentProvider.PAYPAL);
      auth.setProviderOrderId(result.providerOrderId());
      auth.setProviderAuthorizeId(result.providerAuthorizationId());
      auth.setStatus(PaymentAuthorizationStatus.AUTHORIZED);
      auth.setRawResponse(result.rawResponse());
      auth = authorizationRepo.save(auth);

      intent.setStatus(PaymentIntentStatus.AUTHORIZED);
      intentRepo.save(intent);

      log.info("payment.flow.success flow={} intentId={} paypalOrderId={} paypalAuthId={}",
          flowName, intent.getId(), auth.getProviderOrderId(), auth.getProviderAuthorizeId());

      return AuthorizationOutcome.fromNew(intent, auth);
    } catch (RuntimeException e) {
      // Add intent context and mark as FAILED for post-mortem; keep exception propagation.
      intent.setStatus(PaymentIntentStatus.FAILED);
      intentRepo.save(intent);

      log.warn("payment.flow.failure flow={} intentId={} storeId={} orderId={} msg={}",
          flowName, intent.getId(), attempt.merchantStoreId(), attempt.orderId(), e.getMessage(), e);
      throw e;
    }
  }

  private static void validate(AuthorizationAttempt attempt) {
    if (attempt == null) {
      throw new PaymentExceptions.ValidationException("AuthorizePaymentRequest is required");
    }
    if (attempt.merchantStoreId() == null) {
      throw new PaymentExceptions.ValidationException("merchantStoreId is required");
    }
    if (attempt.orderId() == null) {
      throw new PaymentExceptions.ValidationException("orderId is required");
    }
    if (!StringUtils.hasText(attempt.currency()) || attempt.currency().length() != 3) {
      throw new PaymentExceptions.ValidationException("currency must be a 3-letter ISO code");
    }
    if (attempt.amountMinor() < 0) {
      throw new PaymentExceptions.ValidationException("amountMinor must be >= 0");
    }
    if (!StringUtils.hasText(attempt.idempotencyKey())) {
      throw new PaymentExceptions.ValidationException("idempotencyKey is required");
    }
  }

  /**
   * Output object for the flow: keeps the boundary explicit and avoids leaking JPA entities to controllers.
   */
  public record AuthorizationOutcome(
      java.util.UUID paymentIntentId,
      String provider,
      String status,
      String providerOrderId,
      String providerAuthorizationId,
      java.time.OffsetDateTime createdAt,
      boolean idempotent
  ) {

    static AuthorizationOutcome fromExisting(PaymentIntent intent, PaymentAuthorization auth) {
      return new AuthorizationOutcome(
          intent.getId(),
          intent.getProvider().name(),
          intent.getStatus().name(),
          auth != null ? auth.getProviderOrderId() : null,
          auth != null ? auth.getProviderAuthorizeId() : null,
          intent.getCreatedAt(),
          true
      );
    }

    static AuthorizationOutcome fromNew(PaymentIntent intent, PaymentAuthorization auth) {
      return new AuthorizationOutcome(
          intent.getId(),
          intent.getProvider().name(),
          intent.getStatus().name(),
          auth.getProviderOrderId(),
          auth.getProviderAuthorizeId(),
          intent.getCreatedAt(),
          false
      );
    }
  }
}
