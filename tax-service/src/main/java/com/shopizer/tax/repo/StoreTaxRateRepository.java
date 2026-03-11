package com.shopizer.tax.repo;

import com.shopizer.tax.domain.StoreTaxRate;
import org.springframework.data.jpa.repository.JpaRepository;

/**
 * JPA repository for store tax rates.
 */
public interface StoreTaxRateRepository extends JpaRepository<StoreTaxRate, Long> {
}
