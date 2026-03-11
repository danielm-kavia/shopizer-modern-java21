package com.shopizer.promotions.repo;

import com.shopizer.promotions.domain.CouponDefinition;
import java.util.Optional;
import org.springframework.data.jpa.repository.JpaRepository;

public interface CouponDefinitionRepository extends JpaRepository<CouponDefinition, Long> {
  Optional<CouponDefinition> findByCode(String code);
}
