package com.shopizer.order.web;

import com.shopizer.order.domain.Order;
import com.shopizer.order.domain.OrderItem;
import com.shopizer.order.service.OrderService;
import com.shopizer.order.web.dto.CreateOrderRequest;
import com.shopizer.order.web.dto.OrderResponse;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.Parameter;
import io.swagger.v3.oas.annotations.media.Content;
import io.swagger.v3.oas.annotations.media.Schema;
import io.swagger.v3.oas.annotations.responses.ApiResponse;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.validation.Valid;
import java.util.List;
import java.util.UUID;
import java.util.stream.Collectors;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

/**
 * Order REST API endpoints.
 *
 * Security: All endpoints require a valid Keycloak-issued JWT (configured as Resource Server).
 */
@RestController
@RequestMapping("/api/orders")
@Tag(name = "Orders", description = "Order endpoints (create and query orders)")
public class OrderController {

  private final OrderService orderService;
  private final OrderMapper orderMapper;

  public OrderController(OrderService orderService, OrderMapper orderMapper) {
    this.orderService = orderService;
    this.orderMapper = orderMapper;
  }

  // PUBLIC_INTERFACE
  @PostMapping
  @Operation(
      operationId = "createOrder",
      summary = "Create a new order",
      description = "Creates an order for a given merchant store and customer. Total is computed from line items.",
      responses = {
          @ApiResponse(responseCode = "201", description = "Order created",
              content = @Content(schema = @Schema(implementation = OrderResponse.class))),
          @ApiResponse(responseCode = "400", description = "Validation error"),
          @ApiResponse(responseCode = "401", description = "Unauthorized")
      }
  )
  public ResponseEntity<OrderResponse> createOrder(@Valid @RequestBody CreateOrderRequest request) {
    List<OrderItem> items = request.getItems().stream().map(i -> {
      OrderItem item = new OrderItem();
      item.setProductId(i.getProductId());
      item.setQuantity(i.getQuantity());
      item.setUnitAmount(i.getUnitAmount());
      return item;
    }).collect(Collectors.toList());

    Order created = orderService.createOrder(
        request.getMerchantStoreId(),
        request.getCustomerId(),
        request.getCurrency(),
        items
    );

    return ResponseEntity.status(HttpStatus.CREATED).body(orderMapper.toResponse(created));
  }

  // PUBLIC_INTERFACE
  @GetMapping("/{orderId}")
  @Operation(
      operationId = "getOrder",
      summary = "Get an order by ID",
      description = "Returns the order and its line items.",
      responses = {
          @ApiResponse(responseCode = "200", description = "Order found",
              content = @Content(schema = @Schema(implementation = OrderResponse.class))),
          @ApiResponse(responseCode = "404", description = "Order not found"),
          @ApiResponse(responseCode = "401", description = "Unauthorized")
      }
  )
  public ResponseEntity<OrderResponse> getOrder(
      @Parameter(description = "Order ID") @PathVariable UUID orderId
  ) {
    return orderService.getOrder(orderId)
        .map(o -> ResponseEntity.ok(orderMapper.toResponse(o)))
        .orElseGet(() -> ResponseEntity.notFound().build());
  }

  // PUBLIC_INTERFACE
  @GetMapping
  @Operation(
      operationId = "listOrders",
      summary = "List orders for a customer in a store",
      description = "Lists orders for the given merchantStoreId and customerId, most recent first.",
      responses = {
          @ApiResponse(responseCode = "200", description = "Orders list returned"),
          @ApiResponse(responseCode = "401", description = "Unauthorized")
      }
  )
  public ResponseEntity<List<OrderResponse>> listOrders(
      @Parameter(description = "Merchant store ID") @RequestParam UUID merchantStoreId,
      @Parameter(description = "Customer ID") @RequestParam UUID customerId
  ) {
    List<OrderResponse> resp = orderService.listOrders(merchantStoreId, customerId).stream()
        .map(orderMapper::toResponse)
        .collect(Collectors.toList());
    return ResponseEntity.ok(resp);
  }
}
