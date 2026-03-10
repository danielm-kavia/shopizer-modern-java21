package com.shopizer.payment.domain;

/**
 * Status for a PaymentIntent.
 *
 * Phase 1:
 * - CREATED -> AUTHORIZED or FAILED.
 */
public enum PaymentIntentStatus {
  CREATED,
  AUTHORIZED,
  FAILED
}
