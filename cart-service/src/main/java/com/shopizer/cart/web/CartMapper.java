package com.shopizer.cart.web;

import com.shopizer.cart.domain.Cart;
import com.shopizer.cart.domain.CartItem;
import com.shopizer.cart.web.dto.CartItemResponse;
import com.shopizer.cart.web.dto.CartResponse;

import java.util.Comparator;
import java.util.List;

/**
 * Mapping helpers for cart API.
 */
final class CartMapper {

  private CartMapper() {}

  static CartResponse toResponse(Cart cart) {
    // Sort for stable API outputs
    List<CartItemResponse> items = cart.getItems().stream()
        .sorted(Comparator.comparing(CartItem::getCreatedAt))
        .map(CartMapper::toResponse)
        .toList();

    return new CartResponse(
        cart.getId(),
        cart.getMerchantStoreId(),
        cart.getCustomerId(),
        cart.getCurrency(),
        cart.getStatus(),
        cart.getCreatedAt(),
        cart.getUpdatedAt(),
        items
    );
  }

  static CartItemResponse toResponse(CartItem item) {
    return new CartItemResponse(
        item.getId(),
        item.getProductId(),
        item.getQuantity(),
        item.getCreatedAt(),
        item.getUpdatedAt()
    );
  }
}
