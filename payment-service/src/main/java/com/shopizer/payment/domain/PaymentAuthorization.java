package com.shopizer.payment.domain;

import jakarta.persistence.*;
import java.time.OffsetDateTime;
import java.util.UUID;

/**
 * PaymentAuthorization stores provider-specific authorization details.
 *
 * Phase 1:
 * - provider=PAYPAL
 * - providerOrderId: PayPal order id created prior to authorization
 * - providerAuthorizeId: PayPal authorization id returned by authorize call
 */
@Entity
@Table(
    name = "payment_authorization",
    schema = "shopizer",
    uniqueConstraints = {
        @UniqueConstraint(name = "uq_payment_auth_intent_provider", columnNames = {"payment_intent_id", "provider"})
    }
)
public class PaymentAuthorization {

  @Id
  @GeneratedValue
  private UUID id;

  @ManyToOne(optional = false, fetch = FetchType.LAZY)
  @JoinColumn(name = "payment_intent_id", nullable = false)
  private PaymentIntent paymentIntent;

  @Enumerated(EnumType.STRING)
  @Column(name = "provider", nullable = false, length = 32)
  private PaymentProvider provider;

  @Column(name = "provider_order_id", length = 128)
  private String providerOrderId;

  @Column(name = "provider_authorize_id", length = 128)
  private String providerAuthorizeId;

  @Enumerated(EnumType.STRING)
  @Column(name = "status", nullable = false, length = 32)
  private PaymentAuthorizationStatus status = PaymentAuthorizationStatus.AUTHORIZED;

  @Lob
  @Column(name = "raw_response")
  private String rawResponse;

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

  public PaymentIntent getPaymentIntent() {
    return paymentIntent;
  }

  public void setPaymentIntent(PaymentIntent paymentIntent) {
    this.paymentIntent = paymentIntent;
  }

  public PaymentProvider getProvider() {
    return provider;
  }

  public void setProvider(PaymentProvider provider) {
    this.provider = provider;
  }

  public String getProviderOrderId() {
    return providerOrderId;
  }

  public void setProviderOrderId(String providerOrderId) {
    this.providerOrderId = providerOrderId;
  }

  public String getProviderAuthorizeId() {
    return providerAuthorizeId;
  }

  public void setProviderAuthorizeId(String providerAuthorizeId) {
    this.providerAuthorizeId = providerAuthorizeId;
  }

  public PaymentAuthorizationStatus getStatus() {
    return status;
  }

  public void setStatus(PaymentAuthorizationStatus status) {
    this.status = status;
  }

  public String getRawResponse() {
    return rawResponse;
  }

  public void setRawResponse(String rawResponse) {
    this.rawResponse = rawResponse;
  }

  public OffsetDateTime getCreatedAt() {
    return createdAt;
  }

  public OffsetDateTime getUpdatedAt() {
    return updatedAt;
  }
}
