-- Pricing service baseline schema (shared "shopizer" schema)

CREATE TABLE IF NOT EXISTS shopizer.prices (
  id BIGSERIAL PRIMARY KEY,
  store_code VARCHAR(64) NOT NULL,
  sku VARCHAR(128) NOT NULL,
  currency_code CHAR(3) NOT NULL,
  regular_price NUMERIC(19, 4) NOT NULL,
  sale_price NUMERIC(19, 4) NULL,
  sale_start_at TIMESTAMPTZ NULL,
  sale_end_at TIMESTAMPTZ NULL,
  active BOOLEAN NOT NULL DEFAULT TRUE,
  created_at TIMESTAMPTZ NOT NULL DEFAULT now(),
  updated_at TIMESTAMPTZ NOT NULL DEFAULT now()
);

-- One price record per (store, sku, currency)
CREATE UNIQUE INDEX IF NOT EXISTS ux_prices_store_sku_currency
  ON shopizer.prices(store_code, sku, currency_code);

CREATE INDEX IF NOT EXISTS ix_prices_sku
  ON shopizer.prices(sku);

CREATE INDEX IF NOT EXISTS ix_prices_store
  ON shopizer.prices(store_code);

CREATE INDEX IF NOT EXISTS ix_prices_sale_window
  ON shopizer.prices(sale_start_at, sale_end_at);
