package com.shopizer.payment.repo;

import com.shopizer.payment.domain.PaymentAuthorization;
import com.shopizer.payment.domain.PaymentIntent;
import java.util.Optional;
import java.util.UUID;
import org.springframework.data.jpa.repository.JpaRepository;

/**
 * Repository for PaymentAuthorization.
 */
public interface PaymentAuthorizationRepository extends JpaRepository<PaymentAuthorization, UUID> {

  Optional<PaymentAuthorization> findByPaymentIntent(PaymentIntent paymentIntent);
}
