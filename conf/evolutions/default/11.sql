# noinspection SqlResolveForFile
-- !Ups
ALTER TABLE TurnEffect ADD COLUMN effect varchar(200) DEFAULT NULL;
-- !Downs
ALTER TABLE TurnEffect DROP COLUMN effect;
