package com.shopizer.checkout.client;

import com.shopizer.checkout.client.dto.PricingResolutionResponse;
import com.shopizer.checkout.config.ShopizerClientsProperties;
import com.shopizer.checkout.config.WebClientConfig;
import java.time.Duration;
import java.util.Locale;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.http.HttpHeaders;
import org.springframework.stereotype.Component;
import org.springframework.web.reactive.function.client.WebClient;
import org.springframework.web.reactive.function.client.WebClientResponseException;

/**
 * I/O adapter for pricing-service.
 *
 * <p>Contract:
 * - resolvePrice(...) returns a pricing resolution response or throws PricingServiceClientException with context.
 * - Propagates caller JWT via Authorization header.
 */
@Component
public class PricingServiceClient {

  private static final Logger log = LoggerFactory.getLogger(PricingServiceClient.class);

  private final WebClient webClient;
  private final String baseUrl;

  public PricingServiceClient(WebClient.Builder builder, ShopizerClientsProperties props) {
    this.baseUrl = props.getPricing().getBaseUrl();
    Duration timeout = Duration.ofMillis(props.getPricing().getTimeoutMs());
    this.webClient = builder
        .baseUrl(this.baseUrl)
        .clientConnector(WebClientConfig.connectorWithTimeout(timeout))
        .build();
  }

  // PUBLIC_INTERFACE
  public PricingResolutionResponse resolvePrice(
      String storeCode,
      String sku,
      String currency,
      int quantity,
      String bearerToken
  ) {
    /** Resolve effective price for a SKU from pricing-service, propagating caller JWT. */
    try {
      String normalizedCurrency = normalizeCurrency(currency);

      return webClient.get()
          .uri(uriBuilder -> uriBuilder
              .path("/api/pricing/resolve")
              .queryParam("storeCode", storeCode)
              .queryParam("sku", sku)
              .queryParam("currency", normalizedCurrency)
              .queryParam("qty", quantity)
              .build()
          )
          .header(HttpHeaders.AUTHORIZATION, bearerToken)
          .retrieve()
          .bodyToMono(PricingResolutionResponse.class)
          .block();
    } catch (WebClientResponseException ex) {
      log.warn("PricingServiceClient.resolvePrice failed: status={}, storeCode={}, sku={}, baseUrl={}",
          ex.getStatusCode().value(), storeCode, sku, baseUrl);
      throw new PricingServiceClientException("Failed to resolve price from pricing-service", ex);
    } catch (Exception ex) {
      throw new PricingServiceClientException("Unexpected error calling pricing-service", ex);
    }
  }

  private static String normalizeCurrency(String currency) {
    if (currency == null) {
      return null;
    }
    String trimmed = currency.trim();
    if (trimmed.isBlank()) {
      return null;
    }
    return trimmed.toUpperCase(Locale.ROOT);
  }

  public static class PricingServiceClientException extends RuntimeException {
    public PricingServiceClientException(String message, Throwable cause) {
      super(message, cause);
    }
  }
}
