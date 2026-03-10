package com.shopizer.order.service;

import com.shopizer.order.domain.Order;
import com.shopizer.order.domain.OrderItem;
import com.shopizer.order.domain.OrderStatus;
import com.shopizer.order.domain.PaymentStatus;
import com.shopizer.order.repo.OrderRepository;
import jakarta.transaction.Transactional;
import java.util.List;
import java.util.Optional;
import java.util.UUID;
import org.springframework.stereotype.Service;

/**
 * Application service for order operations.
 */
@Service
public class OrderService {

  private final OrderRepository orderRepository;

  public OrderService(OrderRepository orderRepository) {
    this.orderRepository = orderRepository;
  }

  // PUBLIC_INTERFACE
  @Transactional
  public Order createOrder(UUID merchantStoreId, UUID customerId, String currency, List<OrderItem> items) {
    /** Creates an order with items and calculates the total from line items. */
    Order order = new Order();
    order.setMerchantStoreId(merchantStoreId);
    order.setCustomerId(customerId);
    order.setCurrency(currency);
    order.setStatus(OrderStatus.CREATED);
    order.setPaymentStatus(PaymentStatus.UNPAID);

    long total = 0L;
    if (items != null) {
      for (OrderItem item : items) {
        order.addItem(item);
        total += (long) item.getQuantity() * item.getUnitAmount();
      }
    }
    order.setTotalAmount(total);

    return orderRepository.save(order);
  }

  // PUBLIC_INTERFACE
  public Optional<Order> getOrder(UUID orderId) {
    /** Retrieves a single order by ID. */
    return orderRepository.findById(orderId);
  }

  // PUBLIC_INTERFACE
  public List<Order> listOrders(UUID merchantStoreId, UUID customerId) {
    /** Lists orders for a customer in a store (most recent first). */
    return orderRepository.findByMerchantStoreIdAndCustomerIdOrderByCreatedAtDesc(merchantStoreId, customerId);
  }
}
