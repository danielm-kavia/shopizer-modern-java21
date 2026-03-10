package com.shopizer.catalog.web;

import com.shopizer.catalog.domain.Category;
import com.shopizer.catalog.domain.CategoryDescription;
import com.shopizer.catalog.service.CatalogQueryService;
import com.shopizer.catalog.web.dto.CategoryResponse;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.Parameter;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.validation.constraints.Min;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

import java.util.Comparator;
import java.util.List;
import java.util.UUID;

/**
 * Catalog category endpoints.
 */
@RestController
@RequestMapping("/api/v1/catalog/stores/{storeId}/categories")
@Tag(name = "Catalog")
public class CategoryController {

  private final CatalogQueryService catalogQueryService;

  public CategoryController(CatalogQueryService catalogQueryService) {
    this.catalogQueryService = catalogQueryService;
  }

  // PUBLIC_INTERFACE
  @GetMapping
  @Operation(
      summary = "List categories",
      description = "Lists categories for a given merchant store."
  )
  public Page<CategoryResponse> listCategories(
      @Parameter(description = "Merchant store id", required = true)
      @PathVariable("storeId") UUID storeId,
      @RequestParam(name = "page", defaultValue = "0") @Min(0) int page,
      @RequestParam(name = "size", defaultValue = "50") @Min(1) int size
  ) {
    Page<Category> categories = catalogQueryService.listCategories(storeId, PageRequest.of(page, size));
    return categories.map(this::toResponse);
  }

  // PUBLIC_INTERFACE
  @GetMapping("/{categoryId}")
  @Operation(
      summary = "Get category by id",
      description = "Fetch a category by id (not store-scoped in path). The response includes the owning store id."
  )
  public CategoryResponse getCategory(
      @Parameter(description = "Merchant store id (used for routing/authorization decisions at gateway; not enforced here)", required = true)
      @PathVariable("storeId") UUID storeId,
      @Parameter(description = "Category id", required = true)
      @PathVariable("categoryId") UUID categoryId
  ) {
    Category category = catalogQueryService.getCategory(categoryId);
    return toResponse(category);
  }

  private CategoryResponse toResponse(Category c) {
    List<CategoryResponse.LocalizedText> descriptions = c.getDescriptions().stream()
        .sorted(Comparator.comparing(CategoryDescription::getLanguageId))
        .map(d -> new CategoryResponse.LocalizedText(d.getLanguageId(), d.getName(), d.getDescription(), d.getFriendlyUrl()))
        .toList();

    UUID parentId = c.getParent() != null ? c.getParent().getId() : null;

    return new CategoryResponse(
        c.getId(),
        c.getMerchantStoreId(),
        c.getCode(),
        c.getSortOrder(),
        c.isVisible(),
        parentId,
        descriptions,
        c.getCreatedAt(),
        c.getUpdatedAt()
    );
  }
}
