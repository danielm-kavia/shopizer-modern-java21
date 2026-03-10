package com.shopizer.pricing.config;

import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

import java.time.Clock;

/**
 * Time-related configuration.
 */
@Configuration
public class TimeConfig {

  // PUBLIC_INTERFACE
  @Bean
  public Clock clock() {
    /** Provides system clock; override in tests if needed. */
    return Clock.systemUTC();
  }
}
