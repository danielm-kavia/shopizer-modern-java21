package com.shopizer.payment.service.provider.paypal;

import com.shopizer.payment.config.PayPalProperties;
import com.shopizer.payment.domain.PaymentProvider;
import com.shopizer.payment.service.PaymentExceptions;
import com.shopizer.payment.service.model.AuthorizationAttempt;
import com.shopizer.payment.service.model.AuthorizationResult;
import com.shopizer.payment.service.provider.PaymentAuthorizationProvider;
import java.nio.charset.StandardCharsets;
import java.util.Base64;
import java.util.Map;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.http.MediaType;
import org.springframework.stereotype.Component;
import org.springframework.util.StringUtils;
import org.springframework.web.reactive.function.client.WebClient;

/**
 * PayPal authorize-only provider adapter.
 *
 * NOTE:
 * - This implementation targets PayPal "Orders v2" + "Authorize".
 * - It intentionally stores raw responses for debuggability and future capture/refund work.
 *
 * Failure modes (typical):
 * 1) Missing configuration -> ConfigurationException (startup/runtime)
 * 2) PayPal auth/token failure -> ProviderException
 * 3) PayPal order create/authorize failure -> ProviderException
 */
@Component
public class PayPalAuthorizationProvider implements PaymentAuthorizationProvider {

  private static final Logger log = LoggerFactory.getLogger(PayPalAuthorizationProvider.class);

  private final WebClient webClient;
  private final PayPalProperties props;

  public PayPalAuthorizationProvider(WebClient webClient, PayPalProperties props) {
    this.webClient = webClient;
    this.props = props;
  }

  @Override
  public PaymentProvider provider() {
    return PaymentProvider.PAYPAL;
  }

  @Override
  public AuthorizationResult authorize(AuthorizationAttempt attempt) {
    if (!props.enabled()) {
      throw new PaymentExceptions.ConfigurationException("PayPal provider is disabled (payment.paypal.enabled=false)");
    }
    if (!StringUtils.hasText(props.clientId()) || !StringUtils.hasText(props.clientSecret())) {
      throw new PaymentExceptions.ConfigurationException("PayPal credentials not configured (PAYPAL_CLIENT_ID / PAYPAL_CLIENT_SECRET)");
    }
    if (!StringUtils.hasText(props.baseUrl())) {
      throw new PaymentExceptions.ConfigurationException("PayPal base URL not configured (PAYPAL_BASE_URL)");
    }

    String operation = "PayPalAuthorizeOnly";
    log.info("payment.provider.start op={} storeId={} orderId={} amountMinor={} currency={}",
        operation, attempt.merchantStoreId(), attempt.orderId(), attempt.amountMinor(), attempt.currency());

    try {
      String token = getAccessToken();

      Map<String, Object> orderCreatePayload = PayPalPayloads.buildCreateOrderPayload(attempt.currency(), attempt.amountMinor());
      String orderCreateRaw = webClient.post()
          .uri(props.baseUrl() + "/v2/checkout/orders")
          .contentType(MediaType.APPLICATION_JSON)
          .accept(MediaType.APPLICATION_JSON)
          .headers(h -> h.setBearerAuth(token))
          .bodyValue(orderCreatePayload)
          .retrieve()
          .bodyToMono(String.class)
          .block();

      String orderId = PayPalResponseParsers.extractId(orderCreateRaw);
      if (!StringUtils.hasText(orderId)) {
        throw new PaymentExceptions.ProviderException("PayPal order create succeeded but no order id found in response");
      }

      String authorizeRaw = webClient.post()
          .uri(props.baseUrl() + "/v2/checkout/orders/" + orderId + "/authorize")
          .contentType(MediaType.APPLICATION_JSON)
          .accept(MediaType.APPLICATION_JSON)
          .headers(h -> h.setBearerAuth(token))
          .bodyValue(Map.of())
          .retrieve()
          .bodyToMono(String.class)
          .block();

      String authorizationId = PayPalResponseParsers.extractAuthorizationId(authorizeRaw);

      log.info("payment.provider.success op={} storeId={} orderId={} paypalOrderId={} paypalAuthId={}",
          operation, attempt.merchantStoreId(), attempt.orderId(), orderId, authorizationId);

      // Store both responses for traceability
      String combinedRaw = "{\"createOrder\":" + safeJson(orderCreateRaw) + ",\"authorize\":" + safeJson(authorizeRaw) + "}";
      return new AuthorizationResult(orderId, authorizationId, combinedRaw);
    } catch (PaymentExceptions.ConfigurationException e) {
      throw e;
    } catch (Exception e) {
      log.warn("payment.provider.failure op={} storeId={} orderId={} msg={}",
          operation, attempt.merchantStoreId(), attempt.orderId(), e.getMessage(), e);
      throw new PaymentExceptions.ProviderException("PayPal authorization failed", e);
    }
  }

  private String getAccessToken() {
    String basic = Base64.getEncoder()
        .encodeToString((props.clientId() + ":" + props.clientSecret()).getBytes(StandardCharsets.UTF_8));

    String raw = webClient.post()
        .uri(props.baseUrl() + "/v1/oauth2/token")
        .contentType(MediaType.APPLICATION_FORM_URLENCODED)
        .accept(MediaType.APPLICATION_JSON)
        .headers(h -> h.set("Authorization", "Basic " + basic))
        .bodyValue("grant_type=client_credentials")
        .retrieve()
        .bodyToMono(String.class)
        .block();

    String token = PayPalResponseParsers.extractAccessToken(raw);
    if (!StringUtils.hasText(token)) {
      throw new PaymentExceptions.ProviderException("PayPal token response missing access_token");
    }
    return token;
  }

  /**
   * Ensures a raw string can be embedded into a JSON envelope for storage.
   * If it already looks like JSON, return as-is; otherwise quote it.
   */
  private static String safeJson(String raw) {
    if (raw == null) {
      return "null";
    }
    String trimmed = raw.trim();
    if (trimmed.startsWith("{") || trimmed.startsWith("[")) {
      return trimmed;
    }
    // best-effort quoting
    return "\"" + trimmed.replace("\"", "\\\"") + "\"";
  }
}
