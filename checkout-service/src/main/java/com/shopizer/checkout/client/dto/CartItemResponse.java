package com.shopizer.checkout.client.dto;

import java.util.UUID;

/**
 * Minimal cart item representation used by checkout-service when reading carts.
 *
 * <p>Note: cart-service currently models items by {@code productId}. The checkout flow is implemented
 * in terms of a SKU identifier (for inventory, pricing, shipping, tax). Until services converge on a
 * shared identifier contract, this DTO provides a derived {@link #sku()} accessor based on
 * {@code productId.toString()}.
 */
public record CartItemResponse(UUID productId, int quantity) {

  // PUBLIC_INTERFACE
  /**
   * Returns a SKU-like identifier for this line item.
   *
   * <p>Phase-1 behavior: derived from {@link #productId()} (UUID string). This keeps the checkout
   * orchestration compiling while upstream cart-service does not yet expose a dedicated SKU field.
   *
   * @return a non-null string when {@code productId} is present; otherwise null
   */
  public String sku() {
    return productId != null ? productId.toString() : null;
  }
}
