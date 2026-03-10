package com.shopizer.checkout.client;

import com.shopizer.checkout.client.dto.CartResponse;
import com.shopizer.checkout.config.ShopizerClientsProperties;
import com.shopizer.checkout.config.WebClientConfig;
import java.time.Duration;
import java.util.UUID;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.http.HttpHeaders;
import org.springframework.stereotype.Component;
import org.springframework.web.reactive.function.client.WebClient;
import org.springframework.web.reactive.function.client.WebClientResponseException;

/**
 * I/O adapter for cart-service.
 *
 * Contract:
 * - getCart(cartId, bearerToken) returns a cart or throws a CartServiceClientException with context.
 */
@Component
public class CartServiceClient {

  private static final Logger log = LoggerFactory.getLogger(CartServiceClient.class);

  private final WebClient webClient;
  private final String baseUrl;

  public CartServiceClient(WebClient.Builder builder, ShopizerClientsProperties props) {
    this.baseUrl = props.getCart().getBaseUrl();
    Duration timeout = Duration.ofMillis(props.getCart().getTimeoutMs());
    this.webClient = builder
        .baseUrl(this.baseUrl)
        .clientConnector(WebClientConfig.connectorWithTimeout(timeout))
        .build();
  }

  // PUBLIC_INTERFACE
  public CartResponse getCart(UUID cartId, String bearerToken) {
    /** Fetch cart from cart-service using caller's bearer token for propagation. */
    try {
      return webClient.get()
          .uri("/api/v1/carts/{cartId}", cartId)
          .header(HttpHeaders.AUTHORIZATION, bearerToken)
          .retrieve()
          .bodyToMono(CartResponse.class)
          .block();
    } catch (WebClientResponseException ex) {
      log.warn("CartServiceClient.getCart failed: status={}, cartId={}, baseUrl={}",
          ex.getStatusCode().value(), cartId, baseUrl);
      throw new CartServiceClientException("Failed to fetch cart from cart-service", ex);
    } catch (Exception ex) {
      throw new CartServiceClientException("Unexpected error calling cart-service", ex);
    }
  }

  public static class CartServiceClientException extends RuntimeException {
    public CartServiceClientException(String message, Throwable cause) {
      super(message, cause);
    }
  }
}
