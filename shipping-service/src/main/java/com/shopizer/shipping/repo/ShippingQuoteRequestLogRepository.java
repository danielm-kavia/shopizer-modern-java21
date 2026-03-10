package com.shopizer.shipping.repo;

import java.util.UUID;
import org.springframework.data.jpa.repository.JpaRepository;

/**
 * Repository for quote request logs.
 */
public interface ShippingQuoteRequestLogRepository extends JpaRepository<ShippingQuoteRequestLog, Long> {

  ShippingQuoteRequestLog findFirstByRequestId(UUID requestId);
}
