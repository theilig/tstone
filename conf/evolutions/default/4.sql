# noinspection SqlResolveForFile
-- !Ups
ALTER TABLE Monster ADD COLUMN frequency INTEGER NOT NULL;
ALTER TABLE Monster ADD COLUMN experience INTEGER NOT NULL;

-- !Downs
ALTER TABLE Monster DROP COLUMN frequency;
ALTER TABLE Monster DROP COLUMN experience;

