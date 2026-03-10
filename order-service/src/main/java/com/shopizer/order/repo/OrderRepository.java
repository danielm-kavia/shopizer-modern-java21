package com.shopizer.order.repo;

import com.shopizer.order.domain.Order;
import java.util.List;
import java.util.UUID;
import org.springframework.data.jpa.repository.JpaRepository;

/**
 * Persistence operations for orders.
 */
public interface OrderRepository extends JpaRepository<Order, UUID> {

  // PUBLIC_INTERFACE
  List<Order> findByMerchantStoreIdAndCustomerIdOrderByCreatedAtDesc(UUID merchantStoreId, UUID customerId);
}
