# noinspection SqlResolveForFile
-- !Ups
<<<<<<< Updated upstream
ALTER TABLE TurnEffect ADD COLUMN effect varchar(200) DEFAULT NULL,
=======
ALTER TABLE TurnEffect ADD COLUMN effect varchar(200) DEFAULT NULL;
>>>>>>> Stashed changes
-- !Downs
ALTER TABLE TurnEffect DROP COLUMN effect;