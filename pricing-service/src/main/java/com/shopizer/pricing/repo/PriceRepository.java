package com.shopizer.pricing.repo;

import com.shopizer.pricing.domain.Price;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.Optional;

/**
 * Repository for price records.
 */
public interface PriceRepository extends JpaRepository<Price, Long> {

  Optional<Price> findByStoreCodeAndSkuAndCurrencyCodeAndActiveIsTrue(String storeCode, String sku, String currencyCode);
}
