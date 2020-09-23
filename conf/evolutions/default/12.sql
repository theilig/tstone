# noinspection SqlResolveForFile
-- !Ups
ALTER TABLE Game CHANGE COLUMN state state LONGTEXT NOT NULL;
-- !Downs
ALTER TABLE Game CHANGE COLUMN state state TEXT NOT NULL;
