package com.shopizer.pricing.domain;

import jakarta.persistence.*;
import java.math.BigDecimal;
import java.time.OffsetDateTime;
import java.util.Objects;

/**
 * Price record per (store_code, sku, currency_code).
 *
 * Invariants:
 * - regularPrice is required and non-negative
 * - salePrice is optional; when present, it may be used only within the sale window (if configured)
 * - effective price resolution is handled by PricingResolutionFlow (do not duplicate logic in controllers)
 */
@Entity
@Table(
    name = "prices",
    schema = "shopizer",
    uniqueConstraints = @UniqueConstraint(name = "ux_prices_store_sku_currency", columnNames = {"store_code", "sku", "currency_code"})
)
public class Price {

  @Id
  @GeneratedValue(strategy = GenerationType.IDENTITY)
  private Long id;

  @Column(name = "store_code", nullable = false, length = 64)
  private String storeCode;

  @Column(name = "sku", nullable = false, length = 128)
  private String sku;

  @Column(name = "currency_code", nullable = false, length = 3)
  private String currencyCode;

  @Column(name = "regular_price", nullable = false, precision = 19, scale = 4)
  private BigDecimal regularPrice;

  @Column(name = "sale_price", precision = 19, scale = 4)
  private BigDecimal salePrice;

  @Column(name = "sale_start_at")
  private OffsetDateTime saleStartAt;

  @Column(name = "sale_end_at")
  private OffsetDateTime saleEndAt;

  @Column(name = "active", nullable = false)
  private boolean active = true;

  @Column(name = "created_at", nullable = false, insertable = false, updatable = false)
  private OffsetDateTime createdAt;

  @Column(name = "updated_at", nullable = false, insertable = false, updatable = false)
  private OffsetDateTime updatedAt;

  public Long getId() {
    return id;
  }

  public String getStoreCode() {
    return storeCode;
  }

  public void setStoreCode(String storeCode) {
    this.storeCode = storeCode;
  }

  public String getSku() {
    return sku;
  }

  public void setSku(String sku) {
    this.sku = sku;
  }

  public String getCurrencyCode() {
    return currencyCode;
  }

  public void setCurrencyCode(String currencyCode) {
    this.currencyCode = currencyCode;
  }

  public BigDecimal getRegularPrice() {
    return regularPrice;
  }

  public void setRegularPrice(BigDecimal regularPrice) {
    this.regularPrice = regularPrice;
  }

  public BigDecimal getSalePrice() {
    return salePrice;
  }

  public void setSalePrice(BigDecimal salePrice) {
    this.salePrice = salePrice;
  }

  public OffsetDateTime getSaleStartAt() {
    return saleStartAt;
  }

  public void setSaleStartAt(OffsetDateTime saleStartAt) {
    this.saleStartAt = saleStartAt;
  }

  public OffsetDateTime getSaleEndAt() {
    return saleEndAt;
  }

  public void setSaleEndAt(OffsetDateTime saleEndAt) {
    this.saleEndAt = saleEndAt;
  }

  public boolean isActive() {
    return active;
  }

  public void setActive(boolean active) {
    this.active = active;
  }

  public OffsetDateTime getCreatedAt() {
    return createdAt;
  }

  public OffsetDateTime getUpdatedAt() {
    return updatedAt;
  }

  @Override
  public boolean equals(Object o) {
    if (this == o) return true;
    if (!(o instanceof Price price)) return false;
    return Objects.equals(storeCode, price.storeCode)
        && Objects.equals(sku, price.sku)
        && Objects.equals(currencyCode, price.currencyCode);
  }

  @Override
  public int hashCode() {
    return Objects.hash(storeCode, sku, currencyCode);
  }
}
