package com.shopizer.catalog.web.dto;

import io.swagger.v3.oas.annotations.media.Schema;

import java.time.Instant;
import java.util.List;
import java.util.UUID;

/**
 * Product detail DTO.
 */
@Schema(name = "ProductDetailResponse", description = "Detailed view of a product including localized names/descriptions.")
public record ProductDetailResponse(
    @Schema(description = "Product id")
    UUID id,
    @Schema(description = "Merchant store id that owns this product")
    UUID merchantStoreId,
    @Schema(description = "Product SKU unique within a store")
    String sku,
    @Schema(description = "Product type")
    String type,
    @Schema(description = "Availability flag")
    boolean available,
    @Schema(description = "Localized descriptions")
    List<LocalizedText> descriptions,
    @Schema(description = "Created at timestamp (UTC)")
    Instant createdAt,
    @Schema(description = "Updated at timestamp (UTC)")
    Instant updatedAt
) {

  @Schema(name = "LocalizedText", description = "Localized text block keyed by language id.")
  public record LocalizedText(
      @Schema(description = "Language id", example = "3fa85f64-5717-4562-b3fc-2c963f66afa6")
      UUID languageId,
      @Schema(description = "Display name", example = "Blue T-Shirt")
      String name,
      @Schema(description = "Long description")
      String description,
      @Schema(description = "SEO-friendly URL slug", example = "blue-t-shirt")
      String friendlyUrl
  ) {}
}
