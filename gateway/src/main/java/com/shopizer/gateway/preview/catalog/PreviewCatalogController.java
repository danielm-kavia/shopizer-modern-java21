package com.shopizer.gateway.preview.catalog;

import com.shopizer.gateway.preview.catalog.dto.CategoryResponse;
import com.shopizer.gateway.preview.catalog.dto.PagedResponse;
import com.shopizer.gateway.preview.catalog.dto.ProductDetailResponse;
import com.shopizer.gateway.preview.catalog.dto.ProductSummaryResponse;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.Parameter;
import io.swagger.v3.oas.annotations.tags.Tag;
import org.springframework.http.server.reactive.ServerHttpResponse;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

import java.time.Instant;
import java.util.List;
import java.util.UUID;

/**
 * Preview-only stub for a small subset of catalog-service endpoints.
 *
 * <p>In Kavia preview, only the gateway is started, so downstream calls like
 * http://localhost:8083 fail with connection refused. The gateway routes are configured to forward
 * /api/v1/catalog/** to /__preview/catalog/**, which is handled by this controller.
 */
@RestController
@RequestMapping("/__preview/catalog")
@Tag(
    name = "Catalog (Preview)",
    description = "Preview stub endpoints served by the gateway when downstream services are not running."
)
public class PreviewCatalogController {

  private static final UUID DEFAULT_LANGUAGE_ID = UUID.fromString("00000000-0000-0000-0000-000000000001");
  private static final Instant STUB_TIME = Instant.parse("2024-01-01T00:00:00Z");

  // PUBLIC_INTERFACE
  @GetMapping("/stores/{storeId}/categories")
  @Operation(
      summary = "List categories (preview stub)",
      description = "Preview stub endpoint used when catalog-service is not running. Returns a minimal page-like response."
  )
  public PagedResponse<CategoryResponse> listCategories(
      @Parameter(description = "Merchant store id", required = true)
      @PathVariable("storeId") UUID storeId,
      @RequestParam(name = "page", defaultValue = "0") int page,
      @RequestParam(name = "size", defaultValue = "50") int size,
      ServerHttpResponse response
  ) {
    response.getHeaders().add("X-Preview-Stub", "catalog");

    List<CategoryResponse> all = stubCategoriesFor(storeId);
    return paginate(all, page, size);
  }

  // PUBLIC_INTERFACE
  @GetMapping("/stores/{storeId}/categories/{categoryId}")
  @Operation(
      summary = "Get category by id (preview stub)",
      description = "Preview stub endpoint used when catalog-service is not running."
  )
  public CategoryResponse getCategory(
      @Parameter(description = "Merchant store id", required = true)
      @PathVariable("storeId") UUID storeId,
      @Parameter(description = "Category id", required = true)
      @PathVariable("categoryId") UUID categoryId,
      ServerHttpResponse response
  ) {
    response.getHeaders().add("X-Preview-Stub", "catalog");

    // Deterministic stub set: return the matching item if present; otherwise return a generic placeholder.
    return stubCategoriesFor(storeId).stream()
        .filter(c -> c.id().equals(categoryId))
        .findFirst()
        .orElseGet(() -> new CategoryResponse(
            categoryId,
            storeId,
            "preview-category",
            0,
            true,
            null,
            List.of(new CategoryResponse.LocalizedText(
                DEFAULT_LANGUAGE_ID,
                "Preview Category",
                "Preview stub category",
                "preview-category"
            )),
            STUB_TIME,
            STUB_TIME
        ));
  }

  // PUBLIC_INTERFACE
  @GetMapping("/stores/{storeId}/products")
  @Operation(
      summary = "List products (preview stub)",
      description = "Preview stub endpoint used when catalog-service is not running. Returns a minimal page-like response."
  )
  public PagedResponse<ProductSummaryResponse> listProducts(
      @Parameter(description = "Merchant store id", required = true)
      @PathVariable("storeId") UUID storeId,
      @RequestParam(name = "page", defaultValue = "0") int page,
      @RequestParam(name = "size", defaultValue = "20") int size,
      ServerHttpResponse response
  ) {
    response.getHeaders().add("X-Preview-Stub", "catalog");

    List<ProductSummaryResponse> all = stubProductsFor(storeId);
    return paginate(all, page, size);
  }

  // PUBLIC_INTERFACE
  @GetMapping("/stores/{storeId}/products/{sku}")
  @Operation(
      summary = "Get product by SKU (preview stub)",
      description = "Preview stub endpoint used when catalog-service is not running."
  )
  public ProductDetailResponse getProductBySku(
      @Parameter(description = "Merchant store id", required = true)
      @PathVariable("storeId") UUID storeId,
      @Parameter(description = "Product SKU", required = true)
      @PathVariable("sku") String sku,
      ServerHttpResponse response
  ) {
    response.getHeaders().add("X-Preview-Stub", "catalog");

    ProductSummaryResponse summary = stubProductsFor(storeId).stream()
        .filter(p -> p.sku() != null && p.sku().equalsIgnoreCase(sku))
        .findFirst()
        .orElseGet(() -> new ProductSummaryResponse(
            UUID.nameUUIDFromBytes(("preview:" + storeId + ":" + sku).getBytes()),
            storeId,
            sku,
            "GENERAL",
            true,
            STUB_TIME,
            STUB_TIME
        ));

    return new ProductDetailResponse(
        summary.id(),
        summary.merchantStoreId(),
        summary.sku(),
        summary.type(),
        summary.available(),
        List.of(new ProductDetailResponse.LocalizedText(
            DEFAULT_LANGUAGE_ID,
            "Preview Product " + summary.sku(),
            "Preview stub product for storefront development/testing.",
            "preview-" + slugify(summary.sku())
        )),
        summary.createdAt(),
        summary.updatedAt()
    );
  }

  private static List<CategoryResponse> stubCategoriesFor(UUID storeId) {
    CategoryResponse home = new CategoryResponse(
        UUID.fromString("11111111-1111-1111-1111-111111111111"),
        storeId,
        "home",
        0,
        true,
        null,
        List.of(new CategoryResponse.LocalizedText(DEFAULT_LANGUAGE_ID, "Home", "Default category", "home")),
        STUB_TIME,
        STUB_TIME
    );

    CategoryResponse featured = new CategoryResponse(
        UUID.fromString("22222222-2222-2222-2222-222222222222"),
        storeId,
        "featured",
        10,
        true,
        null,
        List.of(new CategoryResponse.LocalizedText(DEFAULT_LANGUAGE_ID, "Featured", "Featured items", "featured")),
        STUB_TIME,
        STUB_TIME
    );

    CategoryResponse sale = new CategoryResponse(
        UUID.fromString("33333333-3333-3333-3333-333333333333"),
        storeId,
        "sale",
        20,
        true,
        null,
        List.of(new CategoryResponse.LocalizedText(DEFAULT_LANGUAGE_ID, "Sale", "Sale items", "sale")),
        STUB_TIME,
        STUB_TIME
    );

    return List.of(home, featured, sale);
  }

  private static List<ProductSummaryResponse> stubProductsFor(UUID storeId) {
    ProductSummaryResponse p1 = new ProductSummaryResponse(
        UUID.fromString("aaaaaaaa-aaaa-aaaa-aaaa-aaaaaaaaaaaa"),
        storeId,
        "SKU-PREVIEW-001",
        "GENERAL",
        true,
        STUB_TIME,
        STUB_TIME
    );

    ProductSummaryResponse p2 = new ProductSummaryResponse(
        UUID.fromString("bbbbbbbb-bbbb-bbbb-bbbb-bbbbbbbbbbbb"),
        storeId,
        "SKU-PREVIEW-002",
        "GENERAL",
        true,
        STUB_TIME,
        STUB_TIME
    );

    ProductSummaryResponse p3 = new ProductSummaryResponse(
        UUID.fromString("cccccccc-cccc-cccc-cccc-cccccccccccc"),
        storeId,
        "SKU-PREVIEW-003",
        "GENERAL",
        true,
        STUB_TIME,
        STUB_TIME
    );

    return List.of(p1, p2, p3);
  }

  private static <T> PagedResponse<T> paginate(List<T> all, int page, int size) {
    int safeSize = Math.max(1, size);
    int safePage = Math.max(0, page);

    int from = Math.min(all.size(), safePage * safeSize);
    int to = Math.min(all.size(), from + safeSize);

    /*
     * Be forgiving about page numbering: some clients send 1-based page indexes.
     * If the requested page yields an empty slice but the previous page would contain data,
     * shift down by one to avoid returning an empty stub response.
     */
    if (!all.isEmpty() && safePage > 0 && from >= to) {
      int candidatePage = safePage - 1;
      int candidateFrom = Math.min(all.size(), candidatePage * safeSize);
      int candidateTo = Math.min(all.size(), candidateFrom + safeSize);
      if (candidateFrom < candidateTo) {
        safePage = candidatePage;
        from = candidateFrom;
        to = candidateTo;
      }
    }

    List<T> content = all.subList(from, to);

    long totalElements = all.size();
    int totalPages = (int) Math.ceil(totalElements / (double) safeSize);
    boolean first = safePage == 0;
    boolean last = totalPages == 0 || safePage >= totalPages - 1;

    return new PagedResponse<>(content, safePage, safeSize, totalElements, totalPages, first, last);
  }

  private static String slugify(String value) {
    if (value == null || value.isBlank()) {
      return "item";
    }
    return value
        .trim()
        .toLowerCase()
        .replaceAll("[^a-z0-9]+", "-")
        .replaceAll("(^-+|-+$)", "");
  }
}
