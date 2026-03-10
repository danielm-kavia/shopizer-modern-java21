-- 05_migrate_orders.sql
--
-- Purpose:
--   Migrate orders, line items, totals, and status history.
--
-- Legacy tables referenced (expected, adjust as needed):
--   legacy.ORDERS, legacy.ORDER_PRODUCT, legacy.ORDER_TOTAL, legacy.ORDER_STATUS_HISTORY
--
-- Notes:
--   - Legacy orders may store customerId as primitive; we attempt to map it but also keep customer_ref.
--   - currency_code in new schema is required; assumes legacy has CURRENCY or CURRENCY_CODE.
--   - Product references are best-effort: map by PRODUCT_ID; otherwise keep sku/name only.

BEGIN;

-- Orders
INSERT INTO shopizer.orders (
  id,
  merchant_store_id,
  customer_id,
  customer_ref,
  order_status,
  currency_code,
  payment_module_code,
  shipping_module_code,
  billing_address,
  delivery_address
)
SELECT
  gen_random_uuid(),
  mms.new_id,
  mc.new_id,
  o.CUSTOMER_ID::text,
  o.ORDER_STATUS,
  o.CURRENCY_CODE,
  o.PAYMENT_MODULE_CODE,
  o.SHIPPING_MODULE_CODE,
  jsonb_build_object(
    'address', o.BILLING_ADDRESS,
    'city', o.BILLING_CITY,
    'postalCode', o.BILLING_POSTAL_CODE,
    'country', o.BILLING_COUNTRY,
    'state', o.BILLING_STATE,
    'phone', o.BILLING_PHONE
  ),
  jsonb_build_object(
    'address', o.DELIVERY_ADDRESS,
    'city', o.DELIVERY_CITY,
    'postalCode', o.DELIVERY_POSTAL_CODE,
    'country', o.DELIVERY_COUNTRY,
    'state', o.DELIVERY_STATE,
    'phone', o.DELIVERY_PHONE
  )
FROM legacy.ORDERS o
JOIN shopizer_migration.map_merchant_store mms ON mms.legacy_id = o.MERCHANTID
LEFT JOIN shopizer_migration.map_customer mc ON mc.legacy_id = o.CUSTOMER_ID;

INSERT INTO shopizer_migration.map_orders (legacy_id, new_id)
SELECT
  o.ORDER_ID,
  so.id
FROM legacy.ORDERS o
JOIN shopizer_migration.map_merchant_store mms ON mms.legacy_id = o.MERCHANTID
JOIN shopizer.orders so
  ON so.merchant_store_id = mms.new_id
 AND so.customer_ref = o.CUSTOMER_ID::text
 AND so.created_at::date = COALESCE(o.DATE_PURCHASED::date, so.created_at::date)
ON CONFLICT (legacy_id) DO NOTHING;

-- Order products
INSERT INTO shopizer.order_product (
  id,
  order_id,
  product_id,
  sku,
  product_name,
  quantity,
  unit_price
)
SELECT
  gen_random_uuid(),
  mo.new_id,
  mp.new_id,
  op.SKU,
  op.PRODUCT_NAME,
  op.PRODUCT_QUANTITY,
  op.PRODUCT_PRICE
FROM legacy.ORDER_PRODUCT op
JOIN shopizer_migration.map_orders mo ON mo.legacy_id = op.ORDER_ID
LEFT JOIN shopizer_migration.map_product mp ON mp.legacy_id = op.PRODUCT_ID;

-- Order totals
INSERT INTO shopizer.order_total (
  id,
  order_id,
  code,
  title,
  value,
  sort_order
)
SELECT
  gen_random_uuid(),
  mo.new_id,
  ot.CODE,
  ot.TITLE,
  ot.VALUE,
  COALESCE(ot.SORT_ORDER, 0)
FROM legacy.ORDER_TOTAL ot
JOIN shopizer_migration.map_orders mo ON mo.legacy_id = ot.ORDER_ID;

-- Order status history
INSERT INTO shopizer.order_status_history (
  id,
  order_id,
  status,
  comments,
  customer_notified,
  created_at
)
SELECT
  gen_random_uuid(),
  mo.new_id,
  osh.STATUS,
  osh.COMMENTS,
  COALESCE(osh.CUSTOMER_NOTIFIED, false),
  COALESCE(osh.DATE_ADDED, now())
FROM legacy.ORDER_STATUS_HISTORY osh
JOIN shopizer_migration.map_orders mo ON mo.legacy_id = osh.ORDER_ID;

COMMIT;
