-- HT does not store Sight, so expected partner ownership must come from HP.
-- Fill the values below with HP.sightevent.hptid and HP.sight.partner.hptid pairs.
--
-- Example:
--   with expected(sight_event_id, expected_partner_id) as (
--     values
--       (29, 235),
--       (30, 235),
--       (31, 235)
--   )

with expected(sight_event_id, expected_partner_id) as (
  values
    (null::bigint, null::bigint)
)
select se.sight_event_id,
       se.partner_id as actual_partner_id,
       expected.expected_partner_id,
       se.name
from sight_events se
join expected on expected.sight_event_id = se.sight_event_id
where se.partner_id is distinct from expected.expected_partner_id;
