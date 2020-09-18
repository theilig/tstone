# noinspection SqlResolveForFile
-- !Ups
ALTER TABLE Hero ADD COLUMN hero_type VARCHAR(50) NOT NULL;
ALTER TABLE Hero ADD KEY hero_type (hero_type)

-- !Downs
ALTER TABLE Hero DROP COLUMN hero_type;

