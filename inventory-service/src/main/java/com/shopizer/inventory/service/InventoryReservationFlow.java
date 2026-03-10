package com.shopizer.inventory.service;

import com.shopizer.inventory.domain.InventoryStock;
import com.shopizer.inventory.repo.InventoryStockRepository;
import com.shopizer.inventory.service.InventoryExceptions.InsufficientAvailable;
import com.shopizer.inventory.service.InventoryExceptions.InvalidQuantity;
import com.shopizer.inventory.service.InventoryExceptions.NotFound;
import java.util.Objects;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

/**
 * InventoryReservationFlow
 *
 * Canonical flow for synchronous inventory reservation/release.
 *
 * Contract:
 * Inputs:
 *  - storeId: required, must be > 0
 *  - sku: required, non-blank
 *  - quantity: required, must be > 0
 * Outputs:
 *  - Updated InventoryStock snapshot (onHand, reserved) after mutation.
 * Errors:
 *  - InvalidQuantity (400)
 *  - NotFound (404) when SKU not found for store
 *  - InsufficientAvailable (409) when reserve exceeds available (onHand - reserved)
 * Side effects:
 *  - Updates inventory_stock row in DB.
 */
@Service
public class InventoryReservationFlow {

  private static final Logger log = LoggerFactory.getLogger(InventoryReservationFlow.class);

  private final InventoryStockRepository repository;

  public InventoryReservationFlow(InventoryStockRepository repository) {
    this.repository = repository;
  }

  // PUBLIC_INTERFACE
  @Transactional
  public InventoryStock reserve(ReserveRequest request) {
    /** Reserve quantity of stock by increasing reserved. */
    // REQ: REQ-002 - Provide synchronous reserve API (checkout/cart calls inventory to reserve).
    validateRequest(request, "reserve");

    log.info("InventoryReservationFlow.reserve start storeId={} sku={} qty={}",
        request.storeId(), request.sku(), request.quantity());

    InventoryStock stock = repository.findByStoreIdAndSkuForUpdate(request.storeId(), request.sku())
        .orElseThrow(() -> new NotFound("Stock not found for storeId=" + request.storeId() + ", sku=" + request.sku()));

    int available = stock.getOnHand() - stock.getReserved();
    if (request.quantity() > available) {
      // REQ: REQ-003 - Reservation must fail when insufficient available stock.
      throw new InsufficientAvailable("Insufficient available stock. available=" + available + ", requested=" + request.quantity());
    }

    stock.setReserved(stock.getReserved() + request.quantity());
    InventoryStock saved = repository.save(stock);

    log.info("InventoryReservationFlow.reserve success storeId={} sku={} onHand={} reserved={}",
        request.storeId(), request.sku(), saved.getOnHand(), saved.getReserved());
    return saved;
  }

  // PUBLIC_INTERFACE
  @Transactional
  public InventoryStock release(ReleaseRequest request) {
    /** Release quantity of reserved stock by decreasing reserved (floored at 0 via validation). */
    // REQ: REQ-004 - Provide synchronous release API (checkout/cart calls inventory to release).
    validateRequest(request, "release");

    log.info("InventoryReservationFlow.release start storeId={} sku={} qty={}",
        request.storeId(), request.sku(), request.quantity());

    InventoryStock stock = repository.findByStoreIdAndSkuForUpdate(request.storeId(), request.sku())
        .orElseThrow(() -> new NotFound("Stock not found for storeId=" + request.storeId() + ", sku=" + request.sku()));

    if (request.quantity() > stock.getReserved()) {
      throw new InsufficientAvailable("Cannot release more than reserved. reserved=" + stock.getReserved()
          + ", requested=" + request.quantity());
    }

    stock.setReserved(stock.getReserved() - request.quantity());
    InventoryStock saved = repository.save(stock);

    log.info("InventoryReservationFlow.release success storeId={} sku={} onHand={} reserved={}",
        request.storeId(), request.sku(), saved.getOnHand(), saved.getReserved());
    return saved;
  }

  // PUBLIC_INTERFACE
  @Transactional(readOnly = true)
  public InventoryStock getStock(Long storeId, String sku) {
    /** Retrieve current stock snapshot for (storeId, sku). */
    Objects.requireNonNull(storeId, "storeId is required");
    Objects.requireNonNull(sku, "sku is required");
    if (storeId <= 0) {
      throw new InvalidQuantity("storeId must be > 0");
    }
    if (sku.isBlank()) {
      throw new InvalidQuantity("sku must be non-blank");
    }

    return repository.findByStoreIdAndSku(storeId, sku)
        .orElseThrow(() -> new NotFound("Stock not found for storeId=" + storeId + ", sku=" + sku));
  }

  private static void validateRequest(BaseMutationRequest request, String op) {
    if (request == null) {
      throw new InvalidQuantity(op + " request is required");
    }
    if (request.storeId() == null || request.storeId() <= 0) {
      throw new InvalidQuantity("storeId must be > 0");
    }
    if (request.sku() == null || request.sku().isBlank()) {
      throw new InvalidQuantity("sku must be non-blank");
    }
    if (request.quantity() == null || request.quantity() <= 0) {
      throw new InvalidQuantity("quantity must be > 0");
    }
  }

  public sealed interface BaseMutationRequest permits ReserveRequest, ReleaseRequest {
    Long storeId();
    String sku();
    Integer quantity();
  }

  public record ReserveRequest(Long storeId, String sku, Integer quantity) implements BaseMutationRequest {}

  public record ReleaseRequest(Long storeId, String sku, Integer quantity) implements BaseMutationRequest {}
}
