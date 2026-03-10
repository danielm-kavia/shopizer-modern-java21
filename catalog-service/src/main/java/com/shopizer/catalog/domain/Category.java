package com.shopizer.catalog.domain;

import jakarta.persistence.CascadeType;
import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.FetchType;
import jakarta.persistence.Id;
import jakarta.persistence.JoinColumn;
import jakarta.persistence.ManyToOne;
import jakarta.persistence.OneToMany;
import jakarta.persistence.Table;

import java.time.Instant;
import java.util.LinkedHashSet;
import java.util.Set;
import java.util.UUID;

/**
 * Category aggregate root.
 *
 * Invariants:
 * - A Category belongs to exactly one merchant store (merchantStoreId).
 * - parent is optional; if present it references another category in the same store (not enforced here).
 */
@Entity
@Table(name = "category", schema = "shopizer")
public class Category {

  @Id
  @Column(name = "id", nullable = false)
  private UUID id;

  @Column(name = "merchant_store_id", nullable = false)
  private UUID merchantStoreId;

  @Column(name = "code")
  private String code;

  @Column(name = "sort_order", nullable = false)
  private int sortOrder = 0;

  @Column(name = "is_visible", nullable = false)
  private boolean visible = true;

  @ManyToOne(fetch = FetchType.LAZY)
  @JoinColumn(name = "parent_id")
  private Category parent;

  @OneToMany(mappedBy = "category", cascade = CascadeType.ALL, orphanRemoval = true)
  private Set<CategoryDescription> descriptions = new LinkedHashSet<>();

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

  public String getCode() {
    return code;
  }

  public void setCode(String code) {
    this.code = code;
  }

  public int getSortOrder() {
    return sortOrder;
  }

  public void setSortOrder(int sortOrder) {
    this.sortOrder = sortOrder;
  }

  public boolean isVisible() {
    return visible;
  }

  public void setVisible(boolean visible) {
    this.visible = visible;
  }

  public Category getParent() {
    return parent;
  }

  public void setParent(Category parent) {
    this.parent = parent;
  }

  public Set<CategoryDescription> getDescriptions() {
    return descriptions;
  }

  public void setDescriptions(Set<CategoryDescription> descriptions) {
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
