package com.shopizer.cart.service;

import com.shopizer.cart.domain.Cart;
import com.shopizer.cart.repo.CartRepository;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.NoSuchElementException;
import java.util.UUID;

/**
 * Application service for cart operations.
 */
@Service
public class CartService {

  private final CartRepository cartRepository;

  public CartService(CartRepository cartRepository) {
    this.cartRepository = cartRepository;
  }

  // PUBLIC_INTERFACE
  @Transactional
  public Cart getOrCreateActiveCart(UUID merchantStoreId, UUID customerId, String currency) {
    /** Returns existing ACTIVE cart for (store, customer) or creates one. */
    return cartRepository
        .findByMerchantStoreIdAndCustomerIdAndStatus(merchantStoreId, customerId, "ACTIVE")
        .orElseGet(() -> cartRepository.save(new Cart(merchantStoreId, customerId, currency)));
  }

  // PUBLIC_INTERFACE
  @Transactional(readOnly = true)
  public Cart getCart(UUID cartId) {
    /** Fetch cart by id or throw 404-ish exception (mapped by ApiExceptionHandler). */
    return cartRepository.findById(cartId)
        .orElseThrow(() -> new NoSuchElementException("Cart not found: " + cartId));
  }

  // PUBLIC_INTERFACE
  @Transactional
  public Cart addItem(UUID cartId, UUID productId, int quantity) {
    /** Adds quantity of product to cart. */
    Cart cart = getCart(cartId);
    cart.addItem(productId, quantity);
    return cartRepository.save(cart);
  }

  // PUBLIC_INTERFACE
  @Transactional
  public Cart setItemQuantity(UUID cartId, UUID productId, int quantity) {
    /** Sets product quantity (0 removes). */
    Cart cart = getCart(cartId);
    cart.setItemQuantity(productId, quantity);
    return cartRepository.save(cart);
  }

  // PUBLIC_INTERFACE
  @Transactional
  public Cart removeItem(UUID cartId, UUID productId) {
    /** Removes product from cart. */
    Cart cart = getCart(cartId);
    cart.removeItem(productId);
    return cartRepository.save(cart);
  }
}
