package com.shopizer.cart.domain;

import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.FetchType;
import jakarta.persistence.Id;
import jakarta.persistence.JoinColumn;
import jakarta.persistence.ManyToOne;
import jakarta.persistence.PrePersist;
import jakarta.persistence.Table;

import java.time.Instant;
import java.util.UUID;

/**
 * Cart item entity belonging to a {@link Cart}.
 */
@Entity
@Table(name = "cart_item", schema = "shopizer")
public class CartItem {

  @Id
  @Column(name = "id", nullable = false)
  private UUID id;

  @ManyToOne(fetch = FetchType.LAZY, optional = false)
  @JoinColumn(name = "cart_id", nullable = false)
  private Cart cart;

  @Column(name = "product_id", nullable = false)
  private UUID productId;

  @Column(name = "quantity", nullable = false)
  private int quantity;

  @Column(name = "created_at", nullable = false)
  private Instant createdAt;

  @Column(name = "updated_at", nullable = false)
  private Instant updatedAt;

  protected CartItem() {
    // for JPA
  }

  CartItem(Cart cart, UUID productId, int quantity) {
    this.id = UUID.randomUUID();
    this.cart = cart;
    this.productId = productId;
    this.quantity = quantity;
  }

  @PrePersist
  void onCreate() {
    Instant now = Instant.now();
    this.createdAt = now;
    this.updatedAt = now;
    if (this.id == null) {
      this.id = UUID.randomUUID();
    }
  }

  public UUID getId() {
    return id;
  }

  public Cart getCart() {
    return cart;
  }

  public UUID getProductId() {
    return productId;
  }

  public int getQuantity() {
    return quantity;
  }

  void setQuantity(int quantity) {
    this.quantity = quantity;
    this.updatedAt = Instant.now();
  }

  public Instant getCreatedAt() {
    return createdAt;
  }

  public Instant getUpdatedAt() {
    return updatedAt;
  }
}
