-- Flyway Migration: V2__promotions_seed_mvp
-- Service: promotions-service
-- Purpose:
--   Minimal, idempotent coupon seed data.
--
-- Coupon codes are globally unique.

INSERT INTO shopizer.coupon_definition (code, type, percentage_off, active)
VALUES ('SAVE10', 'PERCENTAGE', 10.0000, true)
ON CONFLICT (code) DO NOTHING;
