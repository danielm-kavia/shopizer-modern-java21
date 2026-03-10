package com.shopizer.catalog.repo;

import com.shopizer.catalog.domain.Product;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.Optional;
import java.util.UUID;

/**
 * Product persistence adapter.
 */
public interface ProductRepository extends JpaRepository<Product, UUID> {

  Optional<Product> findByMerchantStoreIdAndSku(UUID merchantStoreId, String sku);

  Page<Product> findByMerchantStoreId(UUID merchantStoreId, Pageable pageable);
}
