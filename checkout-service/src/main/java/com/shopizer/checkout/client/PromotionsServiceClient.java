package com.shopizer.checkout.client;

import com.shopizer.checkout.client.dto.ApplyCouponRequest;
import com.shopizer.checkout.client.dto.ApplyCouponResponse;
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
 * I/O adapter for promotions-service.
 *
 * <p>Contract:
 * - applyCoupon(request, bearerToken) returns apply-coupon response or throws PromotionsServiceClientException.
 * - Propagates caller JWT via Authorization header.
 */
@Component
public class PromotionsServiceClient {

  private static final Logger log = LoggerFactory.getLogger(PromotionsServiceClient.class);

  private final WebClient webClient;
  private final String baseUrl;

  public PromotionsServiceClient(WebClient.Builder builder, ShopizerClientsProperties props) {
    this.baseUrl = props.getPromotions().getBaseUrl();
    Duration timeout = Duration.ofMillis(props.getPromotions().getTimeoutMs());
    this.webClient = builder
        .baseUrl(this.baseUrl)
        .clientConnector(WebClientConfig.connectorWithTimeout(timeout))
        .build();
  }

  // PUBLIC_INTERFACE
  public ApplyCouponResponse applyCoupon(ApplyCouponRequest request, String bearerToken) {
    /** Apply a coupon code by calling promotions-service, propagating caller JWT. */
    try {
      return webClient.post()
          .uri("/api/promotions/coupons/apply")
          .header(HttpHeaders.AUTHORIZATION, bearerToken)
          .contentType(MediaType.APPLICATION_JSON)
          .bodyValue(request)
          .retrieve()
          .bodyToMono(ApplyCouponResponse.class)
          .block();
    } catch (WebClientResponseException ex) {
      log.warn("PromotionsServiceClient.applyCoupon failed: status={}, code={}, baseUrl={}",
          ex.getStatusCode().value(),
          request != null ? request.code() : null,
          baseUrl);
      throw new PromotionsServiceClientException("Failed to apply coupon in promotions-service", ex);
    } catch (Exception ex) {
      throw new PromotionsServiceClientException("Unexpected error calling promotions-service", ex);
    }
  }

  public static class PromotionsServiceClientException extends RuntimeException {
    public PromotionsServiceClientException(String message, Throwable cause) {
      super(message, cause);
    }
  }
}
