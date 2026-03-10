package com.shopizer.payment.service.provider.paypal;

import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.shopizer.payment.service.PaymentExceptions;
import org.springframework.util.StringUtils;

/**
 * Centralized parsing of PayPal JSON responses.
 *
 * Uses Jackson directly to avoid leaking parsing logic across the codebase.
 */
final class PayPalResponseParsers {

  private static final ObjectMapper mapper = new ObjectMapper();

  private PayPalResponseParsers() {}

  static String extractAccessToken(String rawJson) {
    return readField(rawJson, "access_token");
  }

  static String extractId(String rawJson) {
    return readField(rawJson, "id");
  }

  static String extractAuthorizationId(String rawJson) {
    if (!StringUtils.hasText(rawJson)) {
      return null;
    }
    try {
      JsonNode root = mapper.readTree(rawJson);
      // Typical path in authorize response:
      // purchase_units[0].payments.authorizations[0].id
      JsonNode id = root.at("/purchase_units/0/payments/authorizations/0/id");
      if (id.isMissingNode() || id.isNull()) {
        return null;
      }
      return id.asText();
    } catch (Exception e) {
      throw new PaymentExceptions.ProviderException("Unable to parse PayPal authorization id from response", e);
    }
  }

  private static String readField(String rawJson, String field) {
    if (!StringUtils.hasText(rawJson)) {
      return null;
    }
    try {
      JsonNode root = mapper.readTree(rawJson);
      JsonNode value = root.get(field);
      if (value == null || value.isNull()) {
        return null;
      }
      return value.asText();
    } catch (Exception e) {
      throw new PaymentExceptions.ProviderException("Unable to parse PayPal response field: " + field, e);
    }
  }
}
