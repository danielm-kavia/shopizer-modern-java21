package com.shopizer.checkout.client.dto;

import io.swagger.v3.oas.annotations.media.Schema;
import jakarta.validation.constraints.Min;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;

/**
 * DTO to call inventory-service reserve endpoint.
 *
 * <p>Matches inventory-service InventoryDtos.ReserveRequest.
 */
@Schema(name = "InventoryReserveRequest", description = "Reserve stock synchronously for a store+SKU.")
public record InventoryReserveRequest(
    @NotNull @Schema(description = "Store identifier.", example = "1") Long storeId,
    @NotBlank @Schema(description = "SKU identifier.", example = "SKU-ABC-123") String sku,
    @NotNull @Min(1) @Schema(description = "Quantity to reserve (must be > 0).", example = "1") Integer quantity
) {}
