package com.shopizer.cart.repo;

import com.shopizer.cart.domain.Cart;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.Optional;
import java.util.UUID;

/**
 * Data access for {@link Cart}.
 */
public interface CartRepository extends JpaRepository<Cart, UUID> {

  Optional<Cart> findByMerchantStoreIdAndCustomerIdAndStatus(UUID merchantStoreId, UUID customerId, String status);
}
