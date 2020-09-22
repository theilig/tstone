# noinspection SqlResolveForFile
-- !Ups
ALTER TABLE VillageEffect CHANGE COLUMN attribute_modified attribute_modified ENUM('Card','Gold','Purchase','Experience')
-- !Downs
ALTER TABLE VillageEffect CHANGE COLUMN attribute_modified attribute_modified ENUM('Card','Gold','Purchase')

