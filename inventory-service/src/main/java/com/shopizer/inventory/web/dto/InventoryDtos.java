package com.shopizer.inventory.web.dto;

import io.swagger.v3.oas.annotations.media.Schema;
import jakarta.validation.constraints.Min;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;

/**
 * REST DTOs for inventory-service.
 */
public final class InventoryDtos {

  private InventoryDtos() {}

  @Schema(name = "InventoryStockResponse", description = "Current stock snapshot for a SKU in a store.")
  public record InventoryStockResponse(
      @Schema(description = "Store identifier.", example = "1") long storeId,
      @Schema(description = "SKU identifier.", example = "SKU-ABC-123") String sku,
      @Schema(description = "Total on-hand stock.", example = "10") int onHand,
      @Schema(description = "Reserved stock.", example = "2") int reserved,
      @Schema(description = "Derived availability (onHand - reserved).", example = "8") int available
  ) {}

  @Schema(name = "ReserveRequest", description = "Reserve stock synchronously for a store+SKU.")
  public record ReserveRequest(
      @NotNull @Schema(description = "Store identifier.", example = "1") Long storeId,
      @NotBlank @Schema(description = "SKU identifier.", example = "SKU-ABC-123") String sku,
      @NotNull @Min(1) @Schema(description = "Quantity to reserve (must be > 0).", example = "1") Integer quantity
  ) {}

  @Schema(name = "ReleaseRequest", description = "Release reserved stock synchronously for a store+SKU.")
  public record ReleaseRequest(
      @NotNull @Schema(description = "Store identifier.", example = "1") Long storeId,
      @NotBlank @Schema(description = "SKU identifier.", example = "SKU-ABC-123") String sku,
      @NotNull @Min(1) @Schema(description = "Quantity to release (must be > 0).", example = "1") Integer quantity
  ) {}

  @Schema(name = "ApiErrorResponse", description = "Error response payload.")
  public record ApiErrorResponse(
      @Schema(description = "Stable error code for client logic.", example = "INSUFFICIENT_AVAILABLE") String code,
      @Schema(description = "Human readable message.", example = "Insufficient available stock.") String message
  ) {}
}
