package com.shopizer.cart.domain;

import jakarta.persistence.CascadeType;
import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.FetchType;
import jakarta.persistence.Id;
import jakarta.persistence.OneToMany;
import jakarta.persistence.PrePersist;
import jakarta.persistence.PreUpdate;
import jakarta.persistence.Table;

import java.time.Instant;
import java.util.ArrayList;
import java.util.List;
import java.util.UUID;

/**
 * Cart aggregate root.
 *
 * A cart belongs to a merchant store and a customer (identified by customerId as UUID).
 * Items are modeled as {@link CartItem}.
 */
@Entity
@Table(name = "cart", schema = "shopizer")
public class Cart {

  @Id
  @Column(name = "id", nullable = false)
  private UUID id;

  @Column(name = "merchant_store_id", nullable = false)
  private UUID merchantStoreId;

  @Column(name = "customer_id", nullable = false)
  private UUID customerId;

  @Column(name = "currency", nullable = false, length = 3)
  private String currency;

  @Column(name = "status", nullable = false, length = 32)
  private String status;

  @Column(name = "created_at", nullable = false)
  private Instant createdAt;

  @Column(name = "updated_at", nullable = false)
  private Instant updatedAt;

  @OneToMany(mappedBy = "cart", cascade = CascadeType.ALL, orphanRemoval = true, fetch = FetchType.LAZY)
  private List<CartItem> items = new ArrayList<>();

  protected Cart() {
    // for JPA
  }

  public Cart(UUID merchantStoreId, UUID customerId, String currency) {
    this.id = UUID.randomUUID();
    this.merchantStoreId = merchantStoreId;
    this.customerId = customerId;
    this.currency = currency;
    this.status = "ACTIVE";
  }

  @PrePersist
  void onCreate() {
    Instant now = Instant.now();
    this.createdAt = now;
    this.updatedAt = now;
    if (this.id == null) {
      this.id = UUID.randomUUID();
    }
    if (this.status == null) {
      this.status = "ACTIVE";
    }
  }

  @PreUpdate
  void onUpdate() {
    this.updatedAt = Instant.now();
  }

  public UUID getId() {
    return id;
  }

  public UUID getMerchantStoreId() {
    return merchantStoreId;
  }

  public UUID getCustomerId() {
    return customerId;
  }

  public String getCurrency() {
    return currency;
  }

  public String getStatus() {
    return status;
  }

  public Instant getCreatedAt() {
    return createdAt;
  }

  public Instant getUpdatedAt() {
    return updatedAt;
  }

  public List<CartItem> getItems() {
    return items;
  }

  /**
   * Adds quantity of a product to the cart.
   *
   * If item already exists (by productId), increments quantity.
   */
  public void addItem(UUID productId, int quantity) {
    if (quantity <= 0) {
      throw new IllegalArgumentException("quantity must be > 0");
    }
    CartItem existing = items.stream()
        .filter(i -> i.getProductId().equals(productId))
        .findFirst()
        .orElse(null);

    if (existing == null) {
      items.add(new CartItem(this, productId, quantity));
    } else {
      existing.setQuantity(existing.getQuantity() + quantity);
    }
  }

  /**
   * Sets quantity of a product in the cart.
   *
   * If quantity is 0, removes the item.
   */
  public void setItemQuantity(UUID productId, int quantity) {
    CartItem existing = items.stream()
        .filter(i -> i.getProductId().equals(productId))
        .findFirst()
        .orElse(null);

    if (existing == null) {
      if (quantity <= 0) {
        return;
      }
      items.add(new CartItem(this, productId, quantity));
      return;
    }

    if (quantity <= 0) {
      items.remove(existing);
      return;
    }

    existing.setQuantity(quantity);
  }

  public void removeItem(UUID productId) {
    items.removeIf(i -> i.getProductId().equals(productId));
  }
}
