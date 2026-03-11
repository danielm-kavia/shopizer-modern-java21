package com.shopizer.checkout.client;

import com.shopizer.checkout.client.dto.AuthorizePaymentRequest;
import com.shopizer.checkout.client.dto.AuthorizePaymentResponse;
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
 * I/O adapter for payment-service (authorize-only).
 *
 * <p>Contract:
 * <ul>
 *   <li>authorizePayPal(request, bearerToken) returns an authorization response or throws PaymentServiceClientException</li>
 *   <li>Propagates caller JWT via Authorization header</li>
 * </ul>
 */
@Component
public class PaymentServiceClient {

  private static final Logger log = LoggerFactory.getLogger(PaymentServiceClient.class);

  private final WebClient webClient;
  private final String baseUrl;

  public PaymentServiceClient(WebClient.Builder builder, ShopizerClientsProperties props) {
    this.baseUrl = props.getPayment().getBaseUrl();
    Duration timeout = Duration.ofMillis(props.getPayment().getTimeoutMs());
    this.webClient = builder
        .baseUrl(this.baseUrl)
        .clientConnector(WebClientConfig.connectorWithTimeout(timeout))
        .build();
  }

  // PUBLIC_INTERFACE
  public AuthorizePaymentResponse authorizePayPal(AuthorizePaymentRequest request, String bearerToken) {
    /** Authorize a PayPal payment via payment-service, propagating caller JWT. */
    try {
      return webClient.post()
          .uri("/api/payments/paypal/authorize")
          .header(HttpHeaders.AUTHORIZATION, bearerToken)
          .contentType(MediaType.APPLICATION_JSON)
          .bodyValue(request)
          .retrieve()
          .bodyToMono(AuthorizePaymentResponse.class)
          .block();
    } catch (WebClientResponseException ex) {
      log.warn("PaymentServiceClient.authorizePayPal failed: status={}, orderId={}, baseUrl={}",
          ex.getStatusCode().value(),
          request != null ? request.orderId() : null,
          baseUrl);
      throw new PaymentServiceClientException("Failed to authorize payment in payment-service", ex);
    } catch (Exception ex) {
      throw new PaymentServiceClientException("Unexpected error calling payment-service", ex);
    }
  }

  public static class PaymentServiceClientException extends RuntimeException {
    public PaymentServiceClientException(String message, Throwable cause) {
      super(message, cause);
    }
  }
}
