package com.shopizer.payment.domain;

import jakarta.persistence.*;
import java.time.OffsetDateTime;
import java.util.UUID;

/**
 * PaymentIntent is the internal record representing an authorization attempt for an order.
 *
 * Invariants:
 * - amountMinor >= 0
 * - currency is ISO-4217 (3 chars)
 * - idempotencyKey must be stable per "business attempt" to guarantee safe retries
 */
@Entity
@Table(
    name = "payment_intent",
    schema = "shopizer",
    uniqueConstraints = {
        @UniqueConstraint(name = "uq_payment_intent_store_idempotency", columnNames = {"merchant_store_id", "idempotency_key"})
    },
    indexes = {
        @Index(name = "ix_payment_intent_order", columnList = "merchant_store_id, order_id")
    }
)
public class PaymentIntent {

  @Id
  @GeneratedValue
  private UUID id;

  @Column(name = "merchant_store_id", nullable = false)
  private UUID merchantStoreId;

  @Column(name = "order_id", nullable = false)
  private UUID orderId;

  @Column(name = "customer_id")
  private UUID customerId;

  @Enumerated(EnumType.STRING)
  @Column(name = "provider", nullable = false, length = 32)
  private PaymentProvider provider;

  @Column(name = "currency", nullable = false, length = 3)
  private String currency;

  @Column(name = "amount_minor", nullable = false)
  private long amountMinor;

  @Enumerated(EnumType.STRING)
  @Column(name = "status", nullable = false, length = 32)
  private PaymentIntentStatus status = PaymentIntentStatus.CREATED;

  @Column(name = "idempotency_key", nullable = false, length = 128)
  private String idempotencyKey;

  @Column(name = "created_at", nullable = false)
  private OffsetDateTime createdAt;

  @Column(name = "updated_at", nullable = false)
  private OffsetDateTime updatedAt;

  @PrePersist
  void onCreate() {
    OffsetDateTime now = OffsetDateTime.now();
    this.createdAt = now;
    this.updatedAt = now;
  }

  @PreUpdate
  void onUpdate() {
    this.updatedAt = OffsetDateTime.now();
  }

  public UUID getId() {
    return id;
  }

  public UUID getMerchantStoreId() {
    return merchantStoreId;
  }

  public void setMerchantStoreId(UUID merchantStoreId) {
    this.merchantStoreId = merchantStoreId;
  }

  public UUID getOrderId() {
    return orderId;
  }

  public void setOrderId(UUID orderId) {
    this.orderId = orderId;
  }

  public UUID getCustomerId() {
    return customerId;
  }

  public void setCustomerId(UUID customerId) {
    this.customerId = customerId;
  }

  public PaymentProvider getProvider() {
    return provider;
  }

  public void setProvider(PaymentProvider provider) {
    this.provider = provider;
  }

  public String getCurrency() {
    return currency;
  }

  public void setCurrency(String currency) {
    this.currency = currency;
  }

  public long getAmountMinor() {
    return amountMinor;
  }

  public void setAmountMinor(long amountMinor) {
    this.amountMinor = amountMinor;
  }

  public PaymentIntentStatus getStatus() {
    return status;
  }

  public void setStatus(PaymentIntentStatus status) {
    this.status = status;
  }

  public String getIdempotencyKey() {
    return idempotencyKey;
  }

  public void setIdempotencyKey(String idempotencyKey) {
    this.idempotencyKey = idempotencyKey;
  }

  public OffsetDateTime getCreatedAt() {
    return createdAt;
  }

  public OffsetDateTime getUpdatedAt() {
    return updatedAt;
  }
}
