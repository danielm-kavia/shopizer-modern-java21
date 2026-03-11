package com.shopizer.tax.service;

/**
 * Domain exceptions for tax-service.
 */
public final class TaxExceptions {

  private TaxExceptions() {}

  public static class StoreTaxRateNotFoundException extends RuntimeException {
    public StoreTaxRateNotFoundException(Long storeId) {
      super("No tax rate configured for storeId=" + storeId);
    }
  }
}
