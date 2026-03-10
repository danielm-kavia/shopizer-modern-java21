package com.shopizer.order.web.dto;

import io.swagger.v3.oas.annotations.media.Schema;
import java.util.UUID;

/**
 * Response representation of an order line item.
 */
public class OrderItemResponse {

  @Schema(description = "Order item ID")
  private UUID id;

  @Schema(description = "Product ID")
  private UUID productId;

  @Schema(description = "Quantity")
  private int quantity;

  @Schema(description = "Unit amount in minor currency units")
  private long unitAmount;

  public UUID getId() {
    return id;
  }

  public void setId(UUID id) {
    this.id = id;
  }

  public UUID getProductId() {
    return productId;
  }

  public void setProductId(UUID productId) {
    this.productId = productId;
  }

  public int getQuantity() {
    return quantity;
  }

  public void setQuantity(int quantity) {
    this.quantity = quantity;
  }

  public long getUnitAmount() {
    return unitAmount;
  }

  public void setUnitAmount(long unitAmount) {
    this.unitAmount = unitAmount;
  }
}
