package com.shopizer.inventory.repo;

import com.shopizer.inventory.domain.InventoryStock;
import java.util.Optional;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Lock;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

import jakarta.persistence.LockModeType;

public interface InventoryStockRepository extends JpaRepository<InventoryStock, Long> {

  Optional<InventoryStock> findByStoreIdAndSku(Long storeId, String sku);

  @Lock(LockModeType.PESSIMISTIC_WRITE)
  @Query("select s from InventoryStock s where s.storeId = :storeId and s.sku = :sku")
  Optional<InventoryStock> findByStoreIdAndSkuForUpdate(@Param("storeId") Long storeId, @Param("sku") String sku);
}
