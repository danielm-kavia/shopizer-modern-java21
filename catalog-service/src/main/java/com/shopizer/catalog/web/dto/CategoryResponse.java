package com.shopizer.catalog.web.dto;

import io.swagger.v3.oas.annotations.media.Schema;

import java.time.Instant;
import java.util.List;
import java.util.UUID;

/**
 * Category DTO for API responses.
 */
@Schema(name = "CategoryResponse", description = "Category representation including localized names.")
public record CategoryResponse(
    @Schema(description = "Category id")
    UUID id,
    @Schema(description = "Merchant store id that owns this category")
    UUID merchantStoreId,
    @Schema(description = "Optional category code (unique per store when set)")
    String code,
    @Schema(description = "Sort order")
    int sortOrder,
    @Schema(description = "Visibility flag")
    boolean visible,
    @Schema(description = "Parent category id (if any)")
    UUID parentId,
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
      @Schema(description = "Display name")
      String name,
      @Schema(description = "Long description")
      String description,
      @Schema(description = "SEO-friendly URL slug")
      String friendlyUrl
  ) {}
}
