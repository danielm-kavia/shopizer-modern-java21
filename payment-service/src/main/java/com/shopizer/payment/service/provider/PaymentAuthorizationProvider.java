package com.shopizer.payment.service.provider;

import com.shopizer.payment.domain.PaymentProvider;
import com.shopizer.payment.service.model.AuthorizationAttempt;
import com.shopizer.payment.service.model.AuthorizationResult;

/**
 * Adapter interface for payment authorization providers.
 *
 * Contract:
 * - Input: AuthorizationAttempt (validated by flow boundary)
 * - Output: AuthorizationResult (must contain provider identifiers if created)
 * - Errors: throw ProviderException-wrapped runtime exceptions at boundary
 */
public interface PaymentAuthorizationProvider {

  PaymentProvider provider();

  // PUBLIC_INTERFACE
  AuthorizationResult authorize(AuthorizationAttempt attempt);
}
