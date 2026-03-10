package com.shopizer.inventory.domain;

import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.GenerationType;
import jakarta.persistence.Id;
import jakarta.persistence.Table;
import jakarta.persistence.UniqueConstraint;
import jakarta.persistence.Version;
import java.time.OffsetDateTime;

/**
 * Stock aggregate per (storeId, sku).
 *
 * Invariant: 0 <= reserved <= onHand.
 */
@Entity
@Table(
    name = "inventory_stock",
    uniqueConstraints = @UniqueConstraint(name = "ux_inventory_stock_store_sku", columnNames = {"store_id", "sku"})
)
public class InventoryStock {

  @Id
  @GeneratedValue(strategy = GenerationType.IDENTITY)
  private Long id;

  @Column(name = "store_id", nullable = false)
  private Long storeId;

  @Column(name = "sku", nullable = false, length = 64)
  private String sku;

  @Column(name = "on_hand", nullable = false)
  private int onHand;

  @Column(name = "reserved", nullable = false)
  private int reserved;

  @Version
  @Column(name = "version", nullable = false)
  private long version;

  @Column(name = "created_at", nullable = false)
  private OffsetDateTime createdAt;

  @Column(name = "updated_at", nullable = false)
  private OffsetDateTime updatedAt;

  protected InventoryStock() {
    // JPA
  }

  public InventoryStock(Long storeId, String sku, int onHand, int reserved) {
    this.storeId = storeId;
    this.sku = sku;
    this.onHand = onHand;
    this.reserved = reserved;
  }

  public Long getId() {
    return id;
  }

  public Long getStoreId() {
    return storeId;
  }

  public String getSku() {
    return sku;
  }

  public int getOnHand() {
    return onHand;
  }

  public int getReserved() {
    return reserved;
  }

  public OffsetDateTime getCreatedAt() {
    return createdAt;
  }

  public OffsetDateTime getUpdatedAt() {
    return updatedAt;
  }

  public void setOnHand(int onHand) {
    this.onHand = onHand;
  }

  public void setReserved(int reserved) {
    this.reserved = reserved;
  }
}
