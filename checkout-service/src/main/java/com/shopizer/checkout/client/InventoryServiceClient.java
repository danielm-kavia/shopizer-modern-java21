package com.shopizer.checkout.client;

import com.shopizer.checkout.client.dto.InventoryReserveRequest;
import com.shopizer.checkout.client.dto.InventoryStockResponse;
import com.shopizer.checkout.config.ShopizerClientsProperties;
import com.shopizer.checkout.config.WebClientConfig;
import java.time.Duration;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.http.HttpHeaders;
import org.springframework.http.HttpStatus;
import org.springframework.stereotype.Component;
import org.springframework.web.reactive.function.client.WebClient;
import org.springframework.web.reactive.function.client.WebClientResponseException;

/**
 * I/O adapter for inventory-service.
 *
 * <p>Contract:
 * - reserve(request, bearerToken) returns stock snapshot or throws:
 *   - InventoryReserveFailedException for business reserve failures (e.g., insufficient stock)
 *   - InventoryServiceClientException for other client errors
 */
@Component
public class InventoryServiceClient {

  private static final Logger log = LoggerFactory.getLogger(InventoryServiceClient.class);

  private final WebClient webClient;
  private final String baseUrl;

  public InventoryServiceClient(WebClient.Builder builder, ShopizerClientsProperties props) {
    this.baseUrl = props.getInventory().getBaseUrl();
    Duration timeout = Duration.ofMillis(props.getInventory().getTimeoutMs());
    this.webClient = builder
        .baseUrl(this.baseUrl)
        .clientConnector(WebClientConfig.connectorWithTimeout(timeout))
        .build();
  }

  // PUBLIC_INTERFACE
  public InventoryStockResponse reserve(InventoryReserveRequest request, String bearerToken) {
    /** Reserve stock in inventory-service using caller's bearer token for propagation. */
    try {
      return webClient.post()
          .uri("/api/inventory/reserve")
          .header(HttpHeaders.AUTHORIZATION, bearerToken)
          .bodyValue(request)
          .retrieve()
          .bodyToMono(InventoryStockResponse.class)
          .block();
    } catch (WebClientResponseException ex) {
      // inventory-service maps insufficient availability and other domain errors as 409/422-like responses.
      // We treat these as "reserve failed" so the checkout API can return a clean 409.
      if (ex.getStatusCode() == HttpStatus.CONFLICT || ex.getStatusCode() == HttpStatus.UNPROCESSABLE_ENTITY) {
        log.warn("InventoryServiceClient.reserve failed (reserve rejected): status={}, sku={}, qty={}, baseUrl={}",
            ex.getStatusCode().value(), request.sku(), request.quantity(), baseUrl);
        throw new InventoryReserveFailedException("Insufficient inventory for sku=" + request.sku(), ex);
      }

      log.warn("InventoryServiceClient.reserve failed: status={}, sku={}, qty={}, baseUrl={}",
          ex.getStatusCode().value(), request.sku(), request.quantity(), baseUrl);
      throw new InventoryServiceClientException("Failed to reserve inventory in inventory-service", ex);
    } catch (Exception ex) {
      throw new InventoryServiceClientException("Unexpected error calling inventory-service", ex);
    }
  }

  public static class InventoryReserveFailedException extends RuntimeException {
    public InventoryReserveFailedException(String message, Throwable cause) {
      super(message, cause);
    }
  }

  public static class InventoryServiceClientException extends RuntimeException {
    public InventoryServiceClientException(String message, Throwable cause) {
      super(message, cause);
    }
  }
}
