-- Inventory baseline schema
-- REQ: REQ-001 - Persist stock per (store_id, sku) with on_hand and reserved quantities.

CREATE TABLE IF NOT EXISTS inventory_stock (
  id BIGSERIAL PRIMARY KEY,
  store_id BIGINT NOT NULL,
  sku VARCHAR(64) NOT NULL,
  on_hand INTEGER NOT NULL DEFAULT 0,
  reserved INTEGER NOT NULL DEFAULT 0,
  version BIGINT NOT NULL DEFAULT 0,
  created_at TIMESTAMPTZ NOT NULL DEFAULT now(),
  updated_at TIMESTAMPTZ NOT NULL DEFAULT now(),
  CONSTRAINT ux_inventory_stock_store_sku UNIQUE (store_id, sku),
  CONSTRAINT ck_inventory_stock_nonnegative CHECK (on_hand >= 0 AND reserved >= 0),
  CONSTRAINT ck_inventory_stock_reserved_le_onhand CHECK (reserved <= on_hand)
);

CREATE INDEX IF NOT EXISTS ix_inventory_stock_store_id ON inventory_stock (store_id);
CREATE INDEX IF NOT EXISTS ix_inventory_stock_sku ON inventory_stock (sku);
