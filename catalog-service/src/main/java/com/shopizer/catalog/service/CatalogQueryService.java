package com.shopizer.catalog.service;

import com.shopizer.catalog.domain.Category;
import com.shopizer.catalog.domain.Product;
import com.shopizer.catalog.repo.CategoryRepository;
import com.shopizer.catalog.repo.ProductRepository;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Service;

import java.util.NoSuchElementException;
import java.util.UUID;

/**
 * Catalog query flow (read-only).
 *
 * Flow name: CatalogQueryFlow
 * Canonical entrypoint: this service (used by REST controllers).
 *
 * Contracts:
 * - Inputs: validated UUIDs and store scoping identifiers.
 * - Outputs: JPA entities (read-only use). Callers should map to DTOs.
 * - Errors:
 *   - NoSuchElementException when an entity cannot be found in scope.
 * - Side effects: none (read-only).
 */
@Service
public class CatalogQueryService {

  private static final Logger log = LoggerFactory.getLogger(CatalogQueryService.class);

  private final ProductRepository productRepository;
  private final CategoryRepository categoryRepository;

  public CatalogQueryService(ProductRepository productRepository, CategoryRepository categoryRepository) {
    this.productRepository = productRepository;
    this.categoryRepository = categoryRepository;
  }

  // PUBLIC_INTERFACE
  public Product getProductByStoreAndSku(UUID merchantStoreId, String sku) {
    /** Returns a single product by store + SKU or throws NoSuchElementException if not found. */
    log.info("CatalogQueryFlow.start op=getProductByStoreAndSku storeId={} sku={}", merchantStoreId, sku);
    Product p = productRepository.findByMerchantStoreIdAndSku(merchantStoreId, sku)
        .orElseThrow(() -> new NoSuchElementException("Product not found for storeId=" + merchantStoreId + " sku=" + sku));
    log.info("CatalogQueryFlow.end op=getProductByStoreAndSku productId={}", p.getId());
    return p;
  }

  // PUBLIC_INTERFACE
  public Page<Product> listProducts(UUID merchantStoreId, Pageable pageable) {
    /** Lists products for a store as a page. */
    log.info("CatalogQueryFlow.start op=listProducts storeId={} page={} size={}",
        merchantStoreId, pageable.getPageNumber(), pageable.getPageSize());
    Page<Product> page = productRepository.findByMerchantStoreId(merchantStoreId, pageable);
    log.info("CatalogQueryFlow.end op=listProducts count={}", page.getNumberOfElements());
    return page;
  }

  // PUBLIC_INTERFACE
  public Page<Category> listCategories(UUID merchantStoreId, Pageable pageable) {
    /** Lists categories for a store as a page. */
    log.info("CatalogQueryFlow.start op=listCategories storeId={} page={} size={}",
        merchantStoreId, pageable.getPageNumber(), pageable.getPageSize());
    Page<Category> page = categoryRepository.findByMerchantStoreId(merchantStoreId, pageable);
    log.info("CatalogQueryFlow.end op=listCategories count={}", page.getNumberOfElements());
    return page;
  }

  // PUBLIC_INTERFACE
  public Category getCategory(UUID categoryId) {
    /** Returns a category by id or throws NoSuchElementException if not found. */
    log.info("CatalogQueryFlow.start op=getCategory categoryId={}", categoryId);
    Category c = categoryRepository.findById(categoryId)
        .orElseThrow(() -> new NoSuchElementException("Category not found id=" + categoryId));
    log.info("CatalogQueryFlow.end op=getCategory categoryId={}", c.getId());
    return c;
  }
}
