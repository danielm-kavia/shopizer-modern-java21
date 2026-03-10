package com.shopizer.catalog.domain;

import jakarta.persistence.CascadeType;
import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.FetchType;
import jakarta.persistence.Id;
import jakarta.persistence.OneToMany;
import jakarta.persistence.Table;

import java.time.Instant;
import java.util.LinkedHashSet;
import java.util.Set;
import java.util.UUID;

/**
 * Product aggregate root.
 *
 * Invariants:
 * - Product belongs to exactly one merchant store (merchantStoreId).
 * - (merchant_store_id, sku) is unique at DB level.
 */
@Entity
@Table(name = "product", schema = "shopizer")
public class Product {

  @Id
  @Column(name = "id", nullable = false)
  private UUID id;

  @Column(name = "merchant_store_id", nullable = false)
  private UUID merchantStoreId;

  @Column(name = "sku", nullable = false)
  private String sku;

  @Column(name = "type")
  private String type;

  @Column(name = "is_available", nullable = false)
  private boolean available = true;

  @OneToMany(mappedBy = "product", fetch = FetchType.LAZY, cascade = CascadeType.ALL, orphanRemoval = true)
  private Set<ProductDescription> descriptions = new LinkedHashSet<>();

  @Column(name = "created_at", nullable = false)
  private Instant createdAt;

  @Column(name = "updated_at", nullable = false)
  private Instant updatedAt;

  public UUID getId() {
    return id;
  }

  public void setId(UUID id) {
    this.id = id;
  }

  public UUID getMerchantStoreId() {
    return merchantStoreId;
  }

  public void setMerchantStoreId(UUID merchantStoreId) {
    this.merchantStoreId = merchantStoreId;
  }

  public String getSku() {
    return sku;
  }

  public void setSku(String sku) {
    this.sku = sku;
  }

  public String getType() {
    return type;
  }

  public void setType(String type) {
    this.type = type;
  }

  public boolean isAvailable() {
    return available;
  }

  public void setAvailable(boolean available) {
    this.available = available;
  }

  public Set<ProductDescription> getDescriptions() {
    return descriptions;
  }

  public void setDescriptions(Set<ProductDescription> descriptions) {
    this.descriptions = descriptions;
  }

  public Instant getCreatedAt() {
    return createdAt;
  }

  public void setCreatedAt(Instant createdAt) {
    this.createdAt = createdAt;
  }

  public Instant getUpdatedAt() {
    return updatedAt;
  }

  public void setUpdatedAt(Instant updatedAt) {
    this.updatedAt = updatedAt;
  }
}
