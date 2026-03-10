package com.shopizer.checkout.client.dto;

import io.swagger.v3.oas.annotations.media.Schema;
import jakarta.validation.constraints.Min;
import jakarta.validation.constraints.NotNull;
import java.util.UUID;

/**
 * Minimal create-order item request for order-service.
 *
 * Note: Phase 1 does not integrate catalog pricing, so unitAmount is set to 0.
 */
public class CreateOrderItemRequest {

  @Schema(description = "Product ID", requiredMode = Schema.RequiredMode.REQUIRED)
  @NotNull
  private UUID productId;

  @Schema(description = "Quantity", requiredMode = Schema.RequiredMode.REQUIRED)
  @Min(1)
  private int quantity;

  @Schema(description = "Unit amount (minor currency units). Phase 1 uses 0.", requiredMode = Schema.RequiredMode.REQUIRED)
  private long unitAmount;

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
