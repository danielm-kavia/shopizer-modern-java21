package com.shopizer.checkout.client;

import com.shopizer.checkout.client.dto.TaxCalculateRequest;
import com.shopizer.checkout.client.dto.TaxCalculateResponse;
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
 * I/O adapter for tax-service.
 *
 * <p>Contract:
 * - calculateTax(request, bearerToken) returns calculated tax response or throws TaxServiceClientException.
 * - Propagates caller JWT via Authorization header.
 */
@Component
public class TaxServiceClient {

  private static final Logger log = LoggerFactory.getLogger(TaxServiceClient.class);

  private final WebClient webClient;
  private final String baseUrl;

  public TaxServiceClient(WebClient.Builder builder, ShopizerClientsProperties props) {
    this.baseUrl = props.getTax().getBaseUrl();
    Duration timeout = Duration.ofMillis(props.getTax().getTimeoutMs());
    this.webClient = builder
        .baseUrl(this.baseUrl)
        .clientConnector(WebClientConfig.connectorWithTimeout(timeout))
        .build();
  }

  // PUBLIC_INTERFACE
  public TaxCalculateResponse calculateTax(TaxCalculateRequest request, String bearerToken) {
    /** Calculate tax by calling tax-service, propagating caller JWT. */
    try {
      return webClient.post()
          .uri("/api/tax/calculate")
          .header(HttpHeaders.AUTHORIZATION, bearerToken)
          .contentType(MediaType.APPLICATION_JSON)
          .bodyValue(request)
          .retrieve()
          .bodyToMono(TaxCalculateResponse.class)
          .block();
    } catch (WebClientResponseException ex) {
      log.warn("TaxServiceClient.calculateTax failed: status={}, storeId={}, baseUrl={}",
          ex.getStatusCode().value(),
          request != null ? request.storeId() : null,
          baseUrl);
      throw new TaxServiceClientException("Failed to calculate tax in tax-service", ex);
    } catch (Exception ex) {
      throw new TaxServiceClientException("Unexpected error calling tax-service", ex);
    }
  }

  public static class TaxServiceClientException extends RuntimeException {
    public TaxServiceClientException(String message, Throwable cause) {
      super(message, cause);
    }
  }
}
