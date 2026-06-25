-- HT repair template. Use HP as the source of truth:
-- HP.sightevent.hptid -> HT.sight_events.sight_event_id
-- HP.sight.partner.hptid -> HT.sight_events.partner_id
--
-- Replace the VALUES list with audited pairs before running.

with expected(sight_event_id, expected_partner_id) as (
  values
    (29, 235),
    (30, 235),
    (31, 235)
)
update sight_events se
set partner_id = expected.expected_partner_id
from expected
where expected.sight_event_id = se.sight_event_id
  and se.partner_id is distinct from expected.expected_partner_id;
