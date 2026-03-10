package com.shopizer.order.domain;

/**
 * Payment status for an order.
 */
public enum PaymentStatus {
  UNPAID,
  AUTHORIZED,
  CAPTURED,
  REFUNDED
}
