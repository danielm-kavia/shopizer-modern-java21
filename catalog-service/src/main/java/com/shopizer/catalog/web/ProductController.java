package com.shopizer.catalog.web;

import com.shopizer.catalog.domain.Product;
import com.shopizer.catalog.domain.ProductDescription;
import com.shopizer.catalog.service.CatalogQueryService;
import com.shopizer.catalog.web.dto.ProductDetailResponse;
import com.shopizer.catalog.web.dto.ProductSummaryResponse;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.Parameter;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.validation.constraints.Min;
import jakarta.validation.constraints.NotBlank;
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
 * Catalog product endpoints.
 */
@RestController
@RequestMapping("/api/v1/catalog/stores/{storeId}/products")
@Tag(name = "Catalog")
public class ProductController {

  private final CatalogQueryService catalogQueryService;

  public ProductController(CatalogQueryService catalogQueryService) {
    this.catalogQueryService = catalogQueryService;
  }

  // PUBLIC_INTERFACE
  @GetMapping
  @Operation(
      summary = "List products",
      description = "Lists products for a given merchant store. Results are store-scoped."
  )
  public Page<ProductSummaryResponse> listProducts(
      @Parameter(description = "Merchant store id", required = true)
      @PathVariable("storeId") UUID storeId,
      @Parameter(description = "Page number (0-based)", example = "0")
      @RequestParam(name = "page", defaultValue = "0") @Min(0) int page,
      @Parameter(description = "Page size", example = "20")
      @RequestParam(name = "size", defaultValue = "20") @Min(1) int size
  ) {
    Page<Product> products = catalogQueryService.listProducts(storeId, PageRequest.of(page, size));
    return products.map(p -> new ProductSummaryResponse(
        p.getId(),
        p.getMerchantStoreId(),
        p.getSku(),
        p.getType(),
        p.isAvailable(),
        p.getCreatedAt(),
        p.getUpdatedAt()
    ));
  }

  // PUBLIC_INTERFACE
  @GetMapping("/{sku}")
  @Operation(
      summary = "Get product by SKU",
      description = "Fetches a single product by SKU within a given merchant store."
  )
  public ProductDetailResponse getProductBySku(
      @Parameter(description = "Merchant store id", required = true)
      @PathVariable("storeId") UUID storeId,
      @Parameter(description = "Product SKU", required = true)
      @PathVariable("sku") @NotBlank String sku
  ) {
    Product product = catalogQueryService.getProductByStoreAndSku(storeId, sku);

    List<ProductDetailResponse.LocalizedText> descriptions = product.getDescriptions().stream()
        .sorted(Comparator.comparing(ProductDescription::getLanguageId))
        .map(d -> new ProductDetailResponse.LocalizedText(d.getLanguageId(), d.getName(), d.getDescription(), d.getFriendlyUrl()))
        .toList();

    return new ProductDetailResponse(
        product.getId(),
        product.getMerchantStoreId(),
        product.getSku(),
        product.getType(),
        product.isAvailable(),
        descriptions,
        product.getCreatedAt(),
        product.getUpdatedAt()
    );
  }
}
