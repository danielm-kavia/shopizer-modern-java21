package com.shopizer.order.web.dto;

import com.shopizer.order.domain.OrderStatus;
import com.shopizer.order.domain.PaymentStatus;
import io.swagger.v3.oas.annotations.media.Schema;
import java.time.Instant;
import java.util.List;
import java.util.UUID;

/**
 * Response representation of an order.
 */
public class OrderResponse {

  @Schema(description = "Order ID")
  private UUID id;

  @Schema(description = "Merchant store ID")
  private UUID merchantStoreId;

  @Schema(description = "Customer ID")
  private UUID customerId;

  @Schema(description = "Currency (ISO-4217)")
  private String currency;

  @Schema(description = "Order status")
  private OrderStatus status;

  @Schema(description = "Payment status")
  private PaymentStatus paymentStatus;

  @Schema(description = "Total amount (minor currency units)")
  private long totalAmount;

  @Schema(description = "Created timestamp")
  private Instant createdAt;

  @Schema(description = "Updated timestamp")
  private Instant updatedAt;

  @Schema(description = "Line items")
  private List<OrderItemResponse> items;

  public UUID getId() {
    return id;
  }

  public void setId(UUID id) {
    this.id = id;
  }

  public UUID getMerchantStoreId() {
    return merchantStoreId;
  }

  public void setMerchantStoreId(UUID merchantStoreId) {
    this.merchantStoreId = merchantStoreId;
  }

  public UUID getCustomerId() {
    return customerId;
  }

  public void setCustomerId(UUID customerId) {
    this.customerId = customerId;
  }

  public String getCurrency() {
    return currency;
  }

  public void setCurrency(String currency) {
    this.currency = currency;
  }

  public OrderStatus getStatus() {
    return status;
  }

  public void setStatus(OrderStatus status) {
    this.status = status;
  }

  public PaymentStatus getPaymentStatus() {
    return paymentStatus;
  }

  public void setPaymentStatus(PaymentStatus paymentStatus) {
    this.paymentStatus = paymentStatus;
  }

  public long getTotalAmount() {
    return totalAmount;
  }

  public void setTotalAmount(long totalAmount) {
    this.totalAmount = totalAmount;
  }

  public Instant getCreatedAt() {
    return createdAt;
  }

  public void setCreatedAt(Instant createdAt) {
    this.createdAt = createdAt;
  }

  public Instant getUpdatedAt() {
    return updatedAt;
  }

  public void setUpdatedAt(Instant updatedAt) {
    this.updatedAt = updatedAt;
  }

  public List<OrderItemResponse> getItems() {
    return items;
  }

  public void setItems(List<OrderItemResponse> items) {
    this.items = items;
  }
}
