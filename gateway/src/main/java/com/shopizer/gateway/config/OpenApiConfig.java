package com.shopizer.gateway.config;

import io.swagger.v3.oas.models.OpenAPI;
import io.swagger.v3.oas.models.info.Info;
import io.swagger.v3.oas.models.tags.Tag;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

/** OpenAPI configuration for the gateway. */
@Configuration
public class OpenApiConfig {

  // PUBLIC_INTERFACE
  @Bean
  public OpenAPI gatewayOpenApi() {
    /** Defines OpenAPI metadata and tags for the gateway. */
    return new OpenAPI()
        .info(new Info()
            .title("Shopizer Gateway API")
            .description("Edge gateway (Spring Cloud Gateway) for Shopizer modernization.")
            .version("0.1.0-SNAPSHOT"))
        .addTagsItem(new Tag().name("Health").description("Service health endpoints"));
  }
}
