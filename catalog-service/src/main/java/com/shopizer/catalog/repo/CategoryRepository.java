package com.shopizer.catalog.repo;

import com.shopizer.catalog.domain.Category;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.UUID;

/**
 * Category persistence adapter.
 */
public interface CategoryRepository extends JpaRepository<Category, UUID> {

  Page<Category> findByMerchantStoreId(UUID merchantStoreId, Pageable pageable);
}
