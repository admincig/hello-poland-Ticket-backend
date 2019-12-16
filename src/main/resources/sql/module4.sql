alter table available_ticket_number_association add column parent_id bigint;
update available_ticket_number_association atna set parent_id=parent.id from available_ticket_number_association parent, ticket_pools tp where tp.ticket_pool_definition_id = parent.ticket_pool_definition_id and atna.ticket_pool_id=tp.ticket_pool_id;
