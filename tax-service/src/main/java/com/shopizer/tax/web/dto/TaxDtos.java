package com.shopizer.tax.web.dto;

import io.swagger.v3.oas.annotations.media.Schema;
import jakarta.validation.Valid;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.PositiveOrZero;
import java.math.BigDecimal;
import java.util.List;

/**
 * DTOs for tax calculation API.
 */
public final class TaxDtos {

  private TaxDtos() {}

  @Schema(name = "TaxLine", description = "A line item used for tax calculation.")
  public record TaxLine(
      @NotNull
      @Schema(description = "SKU identifier (not used in Phase 1 calculation, but included for future expansion).", example = "SKU-123")
      String sku,

      @NotNull
      @PositiveOrZero
      @Schema(description = "Quantity of the item.", example = "2")
      Integer quantity,

      @NotNull
      @PositiveOrZero
      @Schema(description = "Unit price for the item, in store currency.", example = "19.99")
      BigDecimal unitPrice
  ) {}

  @Schema(name = "TaxCalculateRequest", description = "Request payload for tax calculation.")
  public record TaxCalculateRequest(
      @NotNull
      @Schema(description = "Store identifier.", example = "1")
      Long storeId,

      @NotNull
      @Valid
      @Schema(description = "Line items to calculate tax for.")
      List<TaxLine> lines
  ) {}

  @Schema(name = "TaxCalculateResponse", description = "Tax calculation result.")
  public record TaxCalculateResponse(
      @NotNull
      @Schema(description = "Store identifier.", example = "1")
      Long storeId,

      @NotNull
      @Schema(description = "Applied store tax rate (fraction).", example = "0.0825")
      BigDecimal taxRate,

      @NotNull
      @Schema(description = "Taxable amount (sum of quantity*unitPrice).", example = "39.98")
      BigDecimal taxableAmount,

      @NotNull
      @Schema(description = "Calculated tax amount.", example = "3.30")
      BigDecimal taxAmount
  ) {}
}
