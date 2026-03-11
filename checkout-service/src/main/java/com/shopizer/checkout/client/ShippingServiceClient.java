package com.shopizer.checkout.client;

import com.shopizer.checkout.client.dto.ShippingQuoteRequest;
import com.shopizer.checkout.client.dto.ShippingQuoteResponse;
import com.shopizer.checkout.config.ShopizerClientsProperties;
import com.shopizer.checkout.config.WebClientConfig;
import java.time.Duration;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.http.HttpHeaders;
import org.springframework.http.MediaType;
import org.springframework.stereotype.Component;
import org.springframework.web.reactive.function.client.WebClient;
import org.springframework.web.reactive.function.client.WebClientResponseException;

/**
 * I/O adapter for shipping-service.
 *
 * <p>Contract:
 * <ul>
 *   <li>getShippingQuotes(request, bearerToken) returns quotes or throws ShippingServiceClientException</li>
 *   <li>Propagates caller JWT via Authorization header</li>
 * </ul>
 */
@Component
public class ShippingServiceClient {

  private static final Logger log = LoggerFactory.getLogger(ShippingServiceClient.class);

  private final WebClient webClient;
  private final String baseUrl;

  public ShippingServiceClient(WebClient.Builder builder, ShopizerClientsProperties props) {
    this.baseUrl = props.getShipping().getBaseUrl();
    Duration timeout = Duration.ofMillis(props.getShipping().getTimeoutMs());
    this.webClient = builder
        .baseUrl(this.baseUrl)
        .clientConnector(WebClientConfig.connectorWithTimeout(timeout))
        .build();
  }

  // PUBLIC_INTERFACE
  public ShippingQuoteResponse getShippingQuotes(ShippingQuoteRequest request, String bearerToken) {
    /** Request shipping quotes from shipping-service, propagating caller JWT. */
    try {
      return webClient.post()
          .uri("/api/shipping/quotes")
          .header(HttpHeaders.AUTHORIZATION, bearerToken)
          .contentType(MediaType.APPLICATION_JSON)
          .bodyValue(request)
          .retrieve()
          .bodyToMono(ShippingQuoteResponse.class)
          .block();
    } catch (WebClientResponseException ex) {
      log.warn("ShippingServiceClient.getShippingQuotes failed: status={}, baseUrl={}",
          ex.getStatusCode().value(), baseUrl);
      throw new ShippingServiceClientException("Failed to get shipping quotes from shipping-service", ex);
    } catch (Exception ex) {
      throw new ShippingServiceClientException("Unexpected error calling shipping-service", ex);
    }
  }

  public static class ShippingServiceClientException extends RuntimeException {
    public ShippingServiceClientException(String message, Throwable cause) {
      super(message, cause);
    }
  }
}
