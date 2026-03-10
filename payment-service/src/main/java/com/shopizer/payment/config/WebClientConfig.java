package com.shopizer.payment.config;

import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.web.reactive.function.client.ExchangeStrategies;
import org.springframework.web.reactive.function.client.WebClient;

/**
 * HTTP client configuration for outbound calls (PayPal).
 */
@Configuration
public class WebClientConfig {

  // PUBLIC_INTERFACE
  @Bean
  public WebClient webClient() {
    /**
     * Creates a shared WebClient instance.
     *
     * Note: payloads returned by PayPal can be verbose; increase buffer limit to avoid truncation.
     */
    int size = 2 * 1024 * 1024; // 2MB
    ExchangeStrategies strategies = ExchangeStrategies.builder()
        .codecs(codecs -> codecs.defaultCodecs().maxInMemorySize(size))
        .build();

    return WebClient.builder()
        .exchangeStrategies(strategies)
        .build();
  }
}
