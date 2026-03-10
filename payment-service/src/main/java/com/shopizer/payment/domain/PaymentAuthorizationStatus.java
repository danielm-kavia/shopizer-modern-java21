package com.shopizer.payment.domain;

/**
 * Status for a PaymentAuthorization.
 *
 * Phase 1 is authorize-only, so AUTHORIZED is the expected terminal state on success.
 */
public enum PaymentAuthorizationStatus {
  AUTHORIZED,
  DECLINED,
  ERROR
}
