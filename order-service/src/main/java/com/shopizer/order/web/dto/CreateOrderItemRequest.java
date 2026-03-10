package com.shopizer.order.web.dto;

import io.swagger.v3.oas.annotations.media.Schema;
import jakarta.validation.constraints.Min;
import jakarta.validation.constraints.NotNull;
import java.util.UUID;

/**
 * Request payload for a single order line item.
 */
public class CreateOrderItemRequest {

  @Schema(description = "Product ID", requiredMode = Schema.RequiredMode.REQUIRED)
  @NotNull
  private UUID productId;

  @Schema(description = "Quantity", example = "2", requiredMode = Schema.RequiredMode.REQUIRED)
  @Min(1)
  private int quantity;

  @Schema(description = "Unit amount in minor currency units (e.g., cents)", example = "1299",
      requiredMode = Schema.RequiredMode.REQUIRED)
  @Min(0)
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
