package com.shopizer.shipping.repo;

import jakarta.persistence.*;
import java.time.Instant;
import java.util.UUID;

/**
 * Minimal persistence entity for debugging/observability of quote requests.
 *
 * Note: This is not yet wired into the RateQuoteFlow to keep Phase 1 minimal.
 * It can be introduced via a dedicated adapter service without changing the REST boundary contract.
 */
@Entity
@Table(name = "shipping_quote_request_log", schema = "shopizer")
public class ShippingQuoteRequestLog {

  @Id
  @GeneratedValue(strategy = GenerationType.IDENTITY)
  private Long id;

  @Column(name = "request_id", nullable = false)
  private UUID requestId;

  @Column(name = "customer_id")
  private String customerId;

  @Column(name = "destination_country", nullable = false)
  private String destinationCountry;

  @Column(name = "destination_postal_code")
  private String destinationPostalCode;

  @Column(name = "currency", nullable = false)
  private String currency;

  @Column(name = "total_weight_grams", nullable = false)
  private Integer totalWeightGrams;

  @Column(name = "created_at", nullable = false)
  private Instant createdAt;

  @Column(name = "request_json", nullable = false, columnDefinition = "TEXT")
  private String requestJson;

  @Column(name = "response_json", columnDefinition = "TEXT")
  private String responseJson;

  @PrePersist
  void prePersist() {
    if (createdAt == null) {
      createdAt = Instant.now();
    }
  }

  public Long getId() {
    return id;
  }

  public UUID getRequestId() {
    return requestId;
  }

  public void setRequestId(UUID requestId) {
    this.requestId = requestId;
  }

  public String getCustomerId() {
    return customerId;
  }

  public void setCustomerId(String customerId) {
    this.customerId = customerId;
  }

  public String getDestinationCountry() {
    return destinationCountry;
  }

  public void setDestinationCountry(String destinationCountry) {
    this.destinationCountry = destinationCountry;
  }

  public String getDestinationPostalCode() {
    return destinationPostalCode;
  }

  public void setDestinationPostalCode(String destinationPostalCode) {
    this.destinationPostalCode = destinationPostalCode;
  }

  public String getCurrency() {
    return currency;
  }

  public void setCurrency(String currency) {
    this.currency = currency;
  }

  public Integer getTotalWeightGrams() {
    return totalWeightGrams;
  }

  public void setTotalWeightGrams(Integer totalWeightGrams) {
    this.totalWeightGrams = totalWeightGrams;
  }

  public Instant getCreatedAt() {
    return createdAt;
  }

  public String getRequestJson() {
    return requestJson;
  }

  public void setRequestJson(String requestJson) {
    this.requestJson = requestJson;
  }

  public String getResponseJson() {
    return responseJson;
  }

  public void setResponseJson(String responseJson) {
    this.responseJson = responseJson;
  }
}
