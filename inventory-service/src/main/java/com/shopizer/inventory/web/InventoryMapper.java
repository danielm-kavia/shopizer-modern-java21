package com.shopizer.inventory.web;

import com.shopizer.inventory.domain.InventoryStock;
import com.shopizer.inventory.web.dto.InventoryDtos.InventoryStockResponse;

/**
 * Inventory API mapping helpers.
 */
public final class InventoryMapper {

  private InventoryMapper() {}

  // PUBLIC_INTERFACE
  public static InventoryStockResponse toResponse(InventoryStock stock) {
    /** Maps InventoryStock entity to InventoryStockResponse DTO. */
    int available = stock.getOnHand() - stock.getReserved();
    return new InventoryStockResponse(stock.getStoreId(), stock.getSku(), stock.getOnHand(), stock.getReserved(), available);
  }
}
