package com.shopizer.checkout.client;

import com.shopizer.checkout.client.dto.CreateOrderRequest;
import com.shopizer.checkout.client.dto.OrderResponse;
import com.shopizer.checkout.config.ShopizerClientsProperties;
import com.shopizer.checkout.config.WebClientConfig;
import java.time.Duration;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.http.HttpHeaders;
import org.springframework.stereotype.Component;
import org.springframework.web.reactive.function.client.WebClient;
import org.springframework.web.reactive.function.client.WebClientResponseException;

/**
 * I/O adapter for order-service.
 *
 * Contract:
 * - createOrder(request, bearerToken) returns created order response or throws OrderServiceClientException with context.
 */
@Component
public class OrderServiceClient {

  private static final Logger log = LoggerFactory.getLogger(OrderServiceClient.class);

  private final WebClient webClient;
  private final String baseUrl;

  public OrderServiceClient(WebClient.Builder builder, ShopizerClientsProperties props) {
    this.baseUrl = props.getOrder().getBaseUrl();
    Duration timeout = Duration.ofMillis(props.getOrder().getTimeoutMs());
    this.webClient = builder
        .baseUrl(this.baseUrl)
        .clientConnector(WebClientConfig.connectorWithTimeout(timeout))
        .build();
  }

  // PUBLIC_INTERFACE
  public OrderResponse createOrder(CreateOrderRequest request, String bearerToken) {
    /** Create an order in order-service using caller's bearer token for propagation. */
    try {
      return webClient.post()
          .uri("/api/orders")
          .header(HttpHeaders.AUTHORIZATION, bearerToken)
          .bodyValue(request)
          .retrieve()
          .bodyToMono(OrderResponse.class)
          .block();
    } catch (WebClientResponseException ex) {
      log.warn("OrderServiceClient.createOrder failed: status={}, baseUrl={}", ex.getStatusCode().value(), baseUrl);
      throw new OrderServiceClientException("Failed to create order in order-service", ex);
    } catch (Exception ex) {
      throw new OrderServiceClientException("Unexpected error calling order-service", ex);
    }
  }

  public static class OrderServiceClientException extends RuntimeException {
    public OrderServiceClientException(String message, Throwable cause) {
      super(message, cause);
    }
  }
}
