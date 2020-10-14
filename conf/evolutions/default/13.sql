# noinspection SqlResolveForFile
-- !Ups
ALTER TABLE TurnEffect CHANGE COLUMN attribute_modified attribute_modified
ENUM('Card', 'Gold', 'Purchase', 'Experience', 'ATT', 'MATT', 'Light', 'Strength', 'Weight', 'Buys', 'Attack', 'Magic Attack')
-- !Downs
