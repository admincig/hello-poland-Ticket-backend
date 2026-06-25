-- Align HelloTicket sight event text columns with Hello Poland limits.
-- Hibernate hbm2ddl=update may not alter existing varchar(255) columns in all deployments.

alter table SIGHT_EVENTS alter column DESCRIPTION type varchar(2500);
alter table SIGHT_EVENTS alter column DIRECTIONS type varchar(1000);
