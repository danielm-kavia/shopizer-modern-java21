package com.shopizer.checkout.config;

import io.netty.channel.ChannelOption;
import io.netty.handler.timeout.ReadTimeoutHandler;
import io.netty.handler.timeout.WriteTimeoutHandler;
import java.time.Duration;
import java.util.concurrent.TimeUnit;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.http.client.reactive.ReactorClientHttpConnector;
import org.springframework.web.reactive.function.client.WebClient;
import reactor.netty.http.client.HttpClient;

/**
 * WebClient configuration for downstream REST calls.
 *
 * Note: Even though we use WebClient, orchestration is synchronous by blocking at the service boundary.
 */
@Configuration
public class WebClientConfig {

  @Bean
  public WebClient.Builder webClientBuilder() {
    return WebClient.builder();
  }

  /**
   * Create a Reactor Netty HttpClient with reasonable defaults and a total response timeout.
   */
  static HttpClient httpClientWithTimeout(Duration responseTimeout) {
    return HttpClient.create()
        .option(ChannelOption.CONNECT_TIMEOUT_MILLIS, (int) responseTimeout.toMillis())
        .responseTimeout(responseTimeout)
        .doOnConnected(conn -> conn
            .addHandlerLast(new ReadTimeoutHandler(responseTimeout.toMillis(), TimeUnit.MILLISECONDS))
            .addHandlerLast(new WriteTimeoutHandler(responseTimeout.toMillis(), TimeUnit.MILLISECONDS))
        );
  }

  // PUBLIC_INTERFACE
  /**
   * Build a {@link ReactorClientHttpConnector} configured with connect/read/write/response timeouts.
   *
   * <p>This is used by the various downstream service clients (cart/inventory/etc) which live in a
   * different package, so it must be {@code public}.
   *
   * @param responseTimeout overall timeout to apply to the underlying Reactor Netty {@link HttpClient}
   * @return a configured {@link ReactorClientHttpConnector}
   */
  public static ReactorClientHttpConnector connectorWithTimeout(Duration responseTimeout) {
    return new ReactorClientHttpConnector(httpClientWithTimeout(responseTimeout));
  }
}
