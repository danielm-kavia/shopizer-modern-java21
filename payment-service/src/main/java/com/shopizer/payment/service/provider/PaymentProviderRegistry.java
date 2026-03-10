package com.shopizer.payment.service.provider;

import com.shopizer.payment.domain.PaymentProvider;
import java.util.List;
import java.util.Map;
import java.util.function.Function;
import java.util.stream.Collectors;
import org.springframework.stereotype.Component;

/**
 * Registry for PaymentAuthorizationProvider implementations.
 *
 * Ensures a single canonical selection mechanism as additional providers are introduced.
 */
@Component
public class PaymentProviderRegistry {

  private final Map<PaymentProvider, PaymentAuthorizationProvider> providers;

  public PaymentProviderRegistry(List<PaymentAuthorizationProvider> providers) {
    this.providers = providers.stream()
        .collect(Collectors.toMap(PaymentAuthorizationProvider::provider, Function.identity()));
  }

  // PUBLIC_INTERFACE
  public PaymentAuthorizationProvider require(PaymentProvider provider) {
    /** Returns the provider implementation or throws if it is not registered. */
    PaymentAuthorizationProvider impl = providers.get(provider);
    if (impl == null) {
      throw new IllegalStateException("No PaymentAuthorizationProvider registered for " + provider);
    }
    return impl;
  }
}
