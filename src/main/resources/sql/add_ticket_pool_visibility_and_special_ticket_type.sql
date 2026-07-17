ALTER TABLE ticket_pool_definitions
  ADD COLUMN IF NOT EXISTS pool_type varchar(32) NOT NULL DEFAULT 'STANDARD';

DO $$
BEGIN
  IF NOT EXISTS (
    SELECT 1
    FROM pg_constraint
    WHERE conname = 'ticket_pool_definitions_pool_type_check'
  ) THEN
    ALTER TABLE ticket_pool_definitions
      ADD CONSTRAINT ticket_pool_definitions_pool_type_check
      CHECK (pool_type IN ('STANDARD', 'PROMOTIONAL'));
  END IF;
END $$;

ALTER TABLE ticket_pool_definitions
  ADD COLUMN IF NOT EXISTS visible_for_partner boolean NOT NULL DEFAULT true;

ALTER TABLE ticket_pool_definitions
  ADD COLUMN IF NOT EXISTS visible_on_portal boolean NOT NULL DEFAULT true;

INSERT INTO ticket_types (code, label, eligible_for_price_from, active, sort_order)
VALUES ('SPECJALNY', 'Specjalny', false, true, 900)
ON CONFLICT (code) DO UPDATE
SET label = EXCLUDED.label,
    eligible_for_price_from = EXCLUDED.eligible_for_price_from,
    active = EXCLUDED.active,
    sort_order = EXCLUDED.sort_order;
