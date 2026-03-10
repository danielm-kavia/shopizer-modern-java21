-- 03_migrate_catalog.sql
--
-- Purpose:
--   Migrate categories, products, and basic availability/pricing.
--
-- Legacy tables referenced (expected, adjust as needed):
--   legacy.CATEGORY, legacy.CATEGORY_DESCRIPTION
--   legacy.PRODUCT, legacy.PRODUCT_DESCRIPTION
--   legacy.PRODUCT_CATEGORY
--   legacy.PRODUCT_AVAILABILITY, legacy.PRODUCT_PRICE, legacy.PRODUCT_PRICE_DESCRIPTION

BEGIN;

-- Categories
INSERT INTO shopizer.category (id, merchant_store_id, code, sort_order, is_visible, parent_id)
SELECT
  gen_random_uuid(),
  mms.new_id AS merchant_store_id,
  c.CODE,
  COALESCE(c.SORT_ORDER, 0),
  COALESCE(c.VISIBLE, true),
  NULL -- parent_id is handled after mapping exists (see below)
FROM legacy.CATEGORY c
JOIN shopizer_migration.map_merchant_store mms ON mms.legacy_id = c.MERCHANT_ID
ON CONFLICT (merchant_store_id, code) DO NOTHING;

INSERT INTO shopizer_migration.map_category (legacy_id, new_id)
SELECT
  c.CATEGORY_ID,
  sc.id
FROM legacy.CATEGORY c
JOIN shopizer_migration.map_merchant_store mms ON mms.legacy_id = c.MERCHANT_ID
JOIN shopizer.category sc ON sc.merchant_store_id = mms.new_id AND sc.code = c.CODE
ON CONFLICT (legacy_id) DO NOTHING;

-- Update parent_id after mappings exist (assumes CATEGORY.PARENT_ID exists)
UPDATE shopizer.category sc
SET parent_id = mp.new_id
FROM legacy.CATEGORY c
JOIN shopizer_migration.map_category mc ON mc.legacy_id = c.CATEGORY_ID
JOIN shopizer_migration.map_category mp ON mp.legacy_id = c.PARENT_ID
WHERE sc.id = mc.new_id
  AND c.PARENT_ID IS NOT NULL;

-- Category descriptions
INSERT INTO shopizer.category_description (id, category_id, language_id, name, description, friendly_url)
SELECT
  gen_random_uuid(),
  mc.new_id,
  ml.new_id,
  cd.NAME,
  cd.DESCRIPTION,
  cd.FRIENDLY_URL
FROM legacy.CATEGORY_DESCRIPTION cd
JOIN shopizer_migration.map_category mc ON mc.legacy_id = cd.CATEGORY_ID
JOIN shopizer_migration.map_language ml ON ml.legacy_id = cd.LANGUAGE_ID
ON CONFLICT (category_id, language_id) DO NOTHING;

-- Products
INSERT INTO shopizer.product (id, merchant_store_id, sku, type, is_available)
SELECT
  gen_random_uuid(),
  mms.new_id,
  p.SKU,
  p.TYPE,
  COALESCE(p.AVAILABLE, true)
FROM legacy.PRODUCT p
JOIN shopizer_migration.map_merchant_store mms ON mms.legacy_id = p.MERCHANT_ID
ON CONFLICT (merchant_store_id, sku) DO NOTHING;

INSERT INTO shopizer_migration.map_product (legacy_id, new_id)
SELECT
  p.PRODUCT_ID,
  sp.id
FROM legacy.PRODUCT p
JOIN shopizer_migration.map_merchant_store mms ON mms.legacy_id = p.MERCHANT_ID
JOIN shopizer.product sp ON sp.merchant_store_id = mms.new_id AND sp.sku = p.SKU
ON CONFLICT (legacy_id) DO NOTHING;

-- Product descriptions
INSERT INTO shopizer.product_description (id, product_id, language_id, name, description, friendly_url)
SELECT
  gen_random_uuid(),
  mp.new_id,
  ml.new_id,
  pd.NAME,
  pd.DESCRIPTION,
  pd.FRIENDLY_URL
FROM legacy.PRODUCT_DESCRIPTION pd
JOIN shopizer_migration.map_product mp ON mp.legacy_id = pd.PRODUCT_ID
JOIN shopizer_migration.map_language ml ON ml.legacy_id = pd.LANGUAGE_ID
ON CONFLICT (product_id, language_id) DO NOTHING;

-- Product-category join
INSERT INTO shopizer.product_category (product_id, category_id)
SELECT
  mp.new_id,
  mc.new_id
FROM legacy.PRODUCT_CATEGORY pc
JOIN shopizer_migration.map_product mp ON mp.legacy_id = pc.PRODUCT_ID
JOIN shopizer_migration.map_category mc ON mc.legacy_id = pc.CATEGORY_ID
ON CONFLICT DO NOTHING;

-- Availability
INSERT INTO shopizer.product_availability (id, product_id, region, quantity, quantity_ordered)
SELECT
  gen_random_uuid(),
  mp.new_id,
  pa.REGION,
  COALESCE(pa.QUANTITY, 0),
  COALESCE(pa.QUANTITY_ORDERED, 0)
FROM legacy.PRODUCT_AVAILABILITY pa
JOIN shopizer_migration.map_product mp ON mp.legacy_id = pa.PRODUCT_ID;

-- Pricing
INSERT INTO shopizer.product_price (
  id,
  availability_id,
  price_code,
  amount,
  special_amount,
  special_start_date,
  special_end_date
)
SELECT
  gen_random_uuid(),
  spa.id AS availability_id,
  COALESCE(pp.PRICE_CODE, 'base'),
  pp.AMOUNT,
  pp.SPECIAL_AMOUNT,
  pp.SPECIAL_START_DATE,
  pp.SPECIAL_END_DATE
FROM legacy.PRODUCT_PRICE pp
JOIN shopizer_migration.map_product mp ON mp.legacy_id = pp.PRODUCT_ID
-- Match to the first availability row created for that product
JOIN LATERAL (
  SELECT id FROM shopizer.product_availability
  WHERE product_id = mp.new_id
  ORDER BY created_at ASC
  LIMIT 1
) spa ON true
ON CONFLICT (availability_id, price_code) DO NOTHING;

COMMIT;
