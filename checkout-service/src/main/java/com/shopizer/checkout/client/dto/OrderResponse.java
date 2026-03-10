package com.shopizer.checkout.client.dto;

import java.util.UUID;

/**
 * Minimal order representation returned by order-service.
 */
public class OrderResponse {
  private UUID id;

  public UUID getId() {
    return id;
  }

  public void setId(UUID id) {
    this.id = id;
  }
}
