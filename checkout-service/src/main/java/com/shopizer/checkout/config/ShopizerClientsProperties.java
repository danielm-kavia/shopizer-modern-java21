package com.shopizer.checkout.config;

import jakarta.validation.Valid;
import jakarta.validation.constraints.Min;
import jakarta.validation.constraints.NotBlank;
import org.springframework.boot.context.properties.ConfigurationProperties;
import org.springframework.validation.annotation.Validated;

/**
 * Typed configuration for downstream service clients used by checkout-service.
 */
@Validated
@ConfigurationProperties(prefix = "shopizer.clients")
public class ShopizerClientsProperties {

  @Valid
  private final ServiceClientProperties cart = new ServiceClientProperties();

  @Valid
  private final ServiceClientProperties order = new ServiceClientProperties();

  @Valid
  private final ServiceClientProperties pricing = new ServiceClientProperties();

  @Valid
  private final ServiceClientProperties tax = new ServiceClientProperties();

  @Valid
  private final ServiceClientProperties promotions = new ServiceClientProperties();

  @Valid
  private final ServiceClientProperties inventory = new ServiceClientProperties();

  @Valid
  private final ServiceClientProperties payment = new ServiceClientProperties();

  public ServiceClientProperties getCart() {
    return cart;
  }

  public ServiceClientProperties getOrder() {
    return order;
  }

  public ServiceClientProperties getPricing() {
    return pricing;
  }

  public ServiceClientProperties getTax() {
    return tax;
  }

  public ServiceClientProperties getPromotions() {
    return promotions;
  }

  public ServiceClientProperties getInventory() {
    return inventory;
  }

  public ServiceClientProperties getPayment() {
    return payment;
  }

  public static class ServiceClientProperties {

    @NotBlank
    private String baseUrl;

    @Min(100)
    private long timeoutMs = 3000;

    public String getBaseUrl() {
      return baseUrl;
    }

    public void setBaseUrl(String baseUrl) {
      this.baseUrl = baseUrl;
    }

    public long getTimeoutMs() {
      return timeoutMs;
    }

    public void setTimeoutMs(long timeoutMs) {
      this.timeoutMs = timeoutMs;
    }
  }
}
