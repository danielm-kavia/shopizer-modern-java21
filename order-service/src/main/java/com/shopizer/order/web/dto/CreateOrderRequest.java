package com.shopizer.order.web.dto;

import io.swagger.v3.oas.annotations.media.Schema;
import jakarta.validation.Valid;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotEmpty;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Pattern;
import java.util.List;
import java.util.UUID;

/**
 * Request payload for creating a new order.
 */
public class CreateOrderRequest {

  @Schema(description = "Merchant store ID", requiredMode = Schema.RequiredMode.REQUIRED)
  @NotNull
  private UUID merchantStoreId;

  @Schema(description = "Customer ID", requiredMode = Schema.RequiredMode.REQUIRED)
  @NotNull
  private UUID customerId;

  @Schema(description = "ISO-4217 currency code", example = "USD", requiredMode = Schema.RequiredMode.REQUIRED)
  @NotBlank
  @Pattern(regexp = "^[A-Z]{3}$", message = "currency must be a 3-letter ISO-4217 code (e.g., USD)")
  private String currency;

  @Schema(description = "Order line items", requiredMode = Schema.RequiredMode.REQUIRED)
  @NotEmpty
  private List<@Valid CreateOrderItemRequest> items;

  public UUID getMerchantStoreId() {
    return merchantStoreId;
  }

  public void setMerchantStoreId(UUID merchantStoreId) {
    this.merchantStoreId = merchantStoreId;
  }

  public UUID getCustomerId() {
    return customerId;
  }

  public void setCustomerId(UUID customerId) {
    this.customerId = customerId;
  }

  public String getCurrency() {
    return currency;
  }

  public void setCurrency(String currency) {
    this.currency = currency;
  }

  public List<CreateOrderItemRequest> getItems() {
    return items;
  }

  public void setItems(List<CreateOrderItemRequest> items) {
    this.items = items;
  }
}
