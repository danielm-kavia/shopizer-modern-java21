package com.shopizer.tax.web;

import com.shopizer.tax.service.TaxCalculationService;
import com.shopizer.tax.web.dto.TaxDtos;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.media.Content;
import io.swagger.v3.oas.annotations.media.Schema;
import io.swagger.v3.oas.annotations.responses.ApiResponse;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.validation.Valid;
import org.springframework.http.MediaType;
import org.springframework.web.bind.annotation.*;

/**
 * Read-only tax calculation endpoints.
 */
@RestController
@RequestMapping(path = "/api/tax", produces = MediaType.APPLICATION_JSON_VALUE)
@Tag(name = "Tax", description = "Tax calculation endpoints")
public class TaxController {

  private final TaxCalculationService taxCalculationService;

  public TaxController(TaxCalculationService taxCalculationService) {
    this.taxCalculationService = taxCalculationService;
  }

  // PUBLIC_INTERFACE
  @PostMapping(path = "/calculate", consumes = MediaType.APPLICATION_JSON_VALUE)
  @Operation(
      summary = "Calculate tax for a cart/order",
      description = "Phase 1: calculates tax using a single store-level percentage rate applied to the taxable amount (sum of quantity*unitPrice).",
      responses = {
          @ApiResponse(
              responseCode = "200",
              description = "Tax calculated successfully",
              content = @Content(schema = @Schema(implementation = TaxDtos.TaxCalculateResponse.class))
          ),
          @ApiResponse(
              responseCode = "401",
              description = "Unauthorized (missing/invalid JWT)"
          ),
          @ApiResponse(
              responseCode = "404",
              description = "No store tax rate configured",
              content = @Content(schema = @Schema(implementation = ApiExceptionHandler.ApiErrorResponse.class))
          )
      }
  )
  public TaxDtos.TaxCalculateResponse calculate(@Valid @RequestBody TaxDtos.TaxCalculateRequest request) {
    /** Calculates tax for the provided cart/order lines. */
    return taxCalculationService.calculate(request);
  }
}
