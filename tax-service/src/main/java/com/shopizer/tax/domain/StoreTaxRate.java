package com.shopizer.tax.domain;

import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.Id;
import jakarta.persistence.Table;
import java.math.BigDecimal;

/**
 * Represents a store-level tax rate.
 *
 * <p>Phase 1 simplification: a single percentage applied to the taxable amount of the order/cart.</p>
 */
@Entity
@Table(name = "store_tax_rate")
public class StoreTaxRate {

  @Id
  @Column(name = "store_id", nullable = false)
  private Long storeId;

  /**
   * Tax rate as a fraction (e.g. 0.0825 for 8.25%).
   */
  @Column(name = "rate", nullable = false, precision = 9, scale = 6)
  private BigDecimal rate;

  protected StoreTaxRate() {
    // for JPA
  }

  public StoreTaxRate(Long storeId, BigDecimal rate) {
    this.storeId = storeId;
    this.rate = rate;
  }

  public Long getStoreId() {
    return storeId;
  }

  public BigDecimal getRate() {
    return rate;
  }
}
