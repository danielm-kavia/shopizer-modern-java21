package com.shopizer.payment.repo;

import com.shopizer.payment.domain.PaymentIntent;
import java.util.Optional;
import java.util.UUID;
import org.springframework.data.jpa.repository.JpaRepository;

/**
 * Repository for PaymentIntent.
 */
public interface PaymentIntentRepository extends JpaRepository<PaymentIntent, UUID> {

  Optional<PaymentIntent> findByMerchantStoreIdAndIdempotencyKey(UUID merchantStoreId, String idempotencyKey);
}
