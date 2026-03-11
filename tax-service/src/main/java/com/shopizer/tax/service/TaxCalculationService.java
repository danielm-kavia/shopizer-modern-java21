package com.shopizer.tax.service;

import com.shopizer.tax.domain.StoreTaxRate;
import com.shopizer.tax.repo.StoreTaxRateRepository;
import com.shopizer.tax.web.dto.TaxDtos;
import java.math.BigDecimal;
import java.math.RoundingMode;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

/**
 * Domain service for tax calculations.
 */
@Service
public class TaxCalculationService {

  private final StoreTaxRateRepository storeTaxRateRepository;

  public TaxCalculationService(StoreTaxRateRepository storeTaxRateRepository) {
    this.storeTaxRateRepository = storeTaxRateRepository;
  }

  /**
   * Calculates tax for the provided request.
   *
   * <p>Phase 1: Applies store-level single rate to the taxable amount derived from line items.</p>
   *
   * @param request tax calculation request
   * @return tax calculation response
   */
  @Transactional(readOnly = true)
  public TaxDtos.TaxCalculateResponse calculate(TaxDtos.TaxCalculateRequest request) {
    StoreTaxRate rate = storeTaxRateRepository.findById(request.storeId())
        .orElseThrow(() -> new TaxExceptions.StoreTaxRateNotFoundException(request.storeId()));

    BigDecimal taxable = request.lines().stream()
        .map(l -> l.unitPrice().multiply(BigDecimal.valueOf(l.quantity())))
        .reduce(BigDecimal.ZERO, BigDecimal::add);

    // Keep money-like rounding behavior stable for phase 1.
    BigDecimal taxAmount = taxable.multiply(rate.getRate())
        .setScale(2, RoundingMode.HALF_UP);

    return new TaxDtos.TaxCalculateResponse(
        request.storeId(),
        rate.getRate(),
        taxable.setScale(2, RoundingMode.HALF_UP),
        taxAmount
    );
  }
}
