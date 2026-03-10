package com.shopizer.catalog.web.dto;

import io.swagger.v3.oas.annotations.media.Schema;

import java.time.Instant;
import java.util.UUID;

/**
 * Product summary DTO for list responses.
 */
@Schema(name = "ProductSummaryResponse", description = "Summary view of a product for catalog listings.")
public record ProductSummaryResponse(
    @Schema(description = "Product id", example = "3fa85f64-5717-4562-b3fc-2c963f66afa6")
    UUID id,

    @Schema(description = "Merchant store id that owns this product", example = "3fa85f64-5717-4562-b3fc-2c963f66afa6")
    UUID merchantStoreId,

    @Schema(description = "Product SKU unique within a store", example = "SKU-12345")
    String sku,

    @Schema(description = "Product type", example = "GENERAL")
    String type,

    @Schema(description = "Availability flag", example = "true")
    boolean available,

    @Schema(description = "Created at timestamp (UTC)")
    Instant createdAt,

    @Schema(description = "Updated at timestamp (UTC)")
    Instant updatedAt
) {}
