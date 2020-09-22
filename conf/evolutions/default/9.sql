# noinspection SqlResolveForFile
-- !Ups
ALTER TABLE Card CHANGE COLUMN frequency frequency INTEGER NOT NULL;
-- !Downs
ALTER TABLE Card CHANGE COLUMN frequency frequency INTEGER;
