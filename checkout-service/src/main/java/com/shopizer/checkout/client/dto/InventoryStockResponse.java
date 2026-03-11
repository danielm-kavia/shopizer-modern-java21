package com.shopizer.checkout.client.dto;

import io.swagger.v3.oas.annotations.media.Schema;

/**
 * Response DTO for inventory-service reserve/release/stock endpoints.
 *
 * <p>Matches inventory-service InventoryDtos.InventoryStockResponse.
 */
@Schema(name = "InventoryStockResponse", description = "Current stock snapshot for a SKU in a store.")
public record InventoryStockResponse(
    @Schema(description = "Store identifier.", example = "1") long storeId,
    @Schema(description = "SKU identifier.", example = "SKU-ABC-123") String sku,
    @Schema(description = "Total on-hand stock.", example = "10") int onHand,
    @Schema(description = "Reserved stock.", example = "2") int reserved,
    @Schema(description = "Derived availability (onHand - reserved).", example = "8") int available
) {}
