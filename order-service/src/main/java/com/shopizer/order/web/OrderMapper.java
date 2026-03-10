package com.shopizer.order.web;

import com.shopizer.order.domain.Order;
import com.shopizer.order.domain.OrderItem;
import com.shopizer.order.web.dto.OrderItemResponse;
import com.shopizer.order.web.dto.OrderResponse;
import java.util.List;
import java.util.stream.Collectors;
import org.springframework.stereotype.Component;

/**
 * Maps Order domain entities to API DTOs.
 */
@Component
public class OrderMapper {

  // PUBLIC_INTERFACE
  public OrderResponse toResponse(Order order) {
    /** Converts an Order JPA entity to an OrderResponse DTO. */
    OrderResponse resp = new OrderResponse();
    resp.setId(order.getId());
    resp.setMerchantStoreId(order.getMerchantStoreId());
    resp.setCustomerId(order.getCustomerId());
    resp.setCurrency(order.getCurrency());
    resp.setStatus(order.getStatus());
    resp.setPaymentStatus(order.getPaymentStatus());
    resp.setTotalAmount(order.getTotalAmount());
    resp.setCreatedAt(order.getCreatedAt());
    resp.setUpdatedAt(order.getUpdatedAt());
    resp.setItems(toItemResponses(order.getItems()));
    return resp;
  }

  private List<OrderItemResponse> toItemResponses(List<OrderItem> items) {
    if (items == null) {
      return List.of();
    }
    return items.stream().map(this::toResponse).collect(Collectors.toList());
  }

  private OrderItemResponse toResponse(OrderItem item) {
    OrderItemResponse resp = new OrderItemResponse();
    resp.setId(item.getId());
    resp.setProductId(item.getProductId());
    resp.setQuantity(item.getQuantity());
    resp.setUnitAmount(item.getUnitAmount());
    return resp;
  }
}
