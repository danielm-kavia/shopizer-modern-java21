package com.shopizer.inventory.web;

import com.shopizer.inventory.domain.InventoryStock;
import com.shopizer.inventory.service.InventoryReservationFlow;
import com.shopizer.inventory.web.dto.InventoryDtos.InventoryStockResponse;
import com.shopizer.inventory.web.dto.InventoryDtos.ReleaseRequest;
import com.shopizer.inventory.web.dto.InventoryDtos.ReserveRequest;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.validation.Valid;
import org.springframework.http.MediaType;
import org.springframework.web.bind.annotation.*;

/**
 * Inventory REST API.
 *
 * Base path is routed via gateway at /api/inventory/**.
 */
@RestController
@RequestMapping(value = "/api/inventory", produces = MediaType.APPLICATION_JSON_VALUE)
@Tag(name = "Inventory")
public class InventoryController {

  private final InventoryReservationFlow flow;

  public InventoryController(InventoryReservationFlow flow) {
    this.flow = flow;
  }

  // PUBLIC_INTERFACE
  @GetMapping("/stock")
  @Operation(
      summary = "Get stock snapshot by store+SKU",
      description = "Returns current on_hand, reserved, and derived available (on_hand - reserved)."
  )
  public InventoryStockResponse getStock(
      @RequestParam("storeId") Long storeId,
      @RequestParam("sku") String sku
  ) {
    InventoryStock stock = flow.getStock(storeId, sku);
    return InventoryMapper.toResponse(stock);
  }

  // PUBLIC_INTERFACE
  @PostMapping("/reserve")
  @Operation(
      summary = "Reserve stock synchronously",
      description = "Increases reserved by quantity if sufficient availability exists (available = on_hand - reserved)."
  )
  public InventoryStockResponse reserve(@Valid @RequestBody ReserveRequest request) {
    // REQ: REQ-002 - Reserve endpoint for synchronous checkout/cart integration.
    InventoryStock stock = flow.reserve(new InventoryReservationFlow.ReserveRequest(
        request.storeId(), request.sku(), request.quantity()
    ));
    return InventoryMapper.toResponse(stock);
  }

  // PUBLIC_INTERFACE
  @PostMapping("/release")
  @Operation(
      summary = "Release reserved stock synchronously",
      description = "Decreases reserved by quantity (cannot release more than currently reserved)."
  )
  public InventoryStockResponse release(@Valid @RequestBody ReleaseRequest request) {
    // REQ: REQ-004 - Release endpoint for synchronous checkout/cart integration.
    InventoryStock stock = flow.release(new InventoryReservationFlow.ReleaseRequest(
        request.storeId(), request.sku(), request.quantity()
    ));
    return InventoryMapper.toResponse(stock);
  }
}
