package com.shopizer.cart.web;

import com.shopizer.cart.domain.Cart;
import com.shopizer.cart.service.CartService;
import com.shopizer.cart.web.dto.AddItemRequest;
import com.shopizer.cart.web.dto.CartResponse;
import com.shopizer.cart.web.dto.CreateOrGetCartRequest;
import com.shopizer.cart.web.dto.SetItemQuantityRequest;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.validation.Valid;
import org.springframework.http.HttpStatus;
import org.springframework.web.bind.annotation.*;

import java.util.UUID;

/**
 * Cart REST API.
 *
 * NOTE: For now, customer identity is passed explicitly (customerId). In a later iteration,
 * this should be derived from JWT claims (subject) to prevent horizontal privilege issues.
 */
@RestController
@RequestMapping("/api/v1/carts")
@Tag(name = "Cart", description = "Cart endpoints (create/get/update carts, items)")
public class CartController {

  private final CartService cartService;

  public CartController(CartService cartService) {
    this.cartService = cartService;
  }

  // PUBLIC_INTERFACE
  @PostMapping("/active")
  @ResponseStatus(HttpStatus.OK)
  @Operation(
      summary = "Get or create active cart",
      description = "Returns existing ACTIVE cart for a (merchantStoreId, customerId) pair, or creates one."
  )
  public CartResponse getOrCreateActiveCart(@Valid @RequestBody CreateOrGetCartRequest request) {
    /** Get or create active cart for the given merchant store and customer. */
    Cart cart = cartService.getOrCreateActiveCart(request.merchantStoreId(), request.customerId(), request.currency());
    return CartMapper.toResponse(cart);
  }

  // PUBLIC_INTERFACE
  @GetMapping("/{cartId}")
  @Operation(summary = "Get cart by id", description = "Fetch a cart and its items by cart id.")
  public CartResponse getCart(@PathVariable UUID cartId) {
    /** Fetch cart by id. */
    return CartMapper.toResponse(cartService.getCart(cartId));
  }

  // PUBLIC_INTERFACE
  @PostMapping("/{cartId}/items")
  @Operation(summary = "Add item to cart", description = "Adds quantity of a product to the cart (increments if exists).")
  public CartResponse addItem(@PathVariable UUID cartId, @Valid @RequestBody AddItemRequest request) {
    /** Add quantity of product to cart. */
    return CartMapper.toResponse(cartService.addItem(cartId, request.productId(), request.quantity()));
  }

  // PUBLIC_INTERFACE
  @PutMapping("/{cartId}/items")
  @Operation(summary = "Set item quantity", description = "Sets quantity of a product in the cart (0 removes the item).")
  public CartResponse setItemQuantity(@PathVariable UUID cartId, @Valid @RequestBody SetItemQuantityRequest request) {
    /** Set quantity of product in cart. */
    return CartMapper.toResponse(cartService.setItemQuantity(cartId, request.productId(), request.quantity()));
  }

  // PUBLIC_INTERFACE
  @DeleteMapping("/{cartId}/items/{productId}")
  @Operation(summary = "Remove item", description = "Removes a product from the cart.")
  public CartResponse removeItem(@PathVariable UUID cartId, @PathVariable UUID productId) {
    /** Remove product from cart. */
    return CartMapper.toResponse(cartService.removeItem(cartId, productId));
  }
}
