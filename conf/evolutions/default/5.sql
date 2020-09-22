# noinspection SqlResolveForFile
-- !Ups
ALTER TABLE DungeonEffect ADD COLUMN need_type varchar(100),
  ADD COLUMN `repeatable` BOOLEAN NOT NULL,
  ADD COLUMN operation ENUM('MULTIPLY','ADD','SUBTRACT','DIVIDE'),
  ADD COLUMN modifier_amount INTEGER,
  ADD COLUMN attribute_modified ENUM('Card', 'ATT', 'MATT', 'Strength', 'Weight', 'Experience');

ALTER TABLE BattleEffect ADD COLUMN need_type varchar(100),
  ADD COLUMN `repeatable` BOOLEAN NOT NULL,
  ADD COLUMN operation ENUM('MULTIPLY','ADD','SUBTRACT','DIVIDE'),
  ADD COLUMN modifier_amount INTEGER,
  ADD COLUMN attribute_modified ENUM('Card', 'ATT', 'MATT', 'Light', 'Strength');

ALTER TABLE VillageEffect ADD COLUMN need_type varchar(100),
  ADD COLUMN `repeatable` BOOLEAN NOT NULL,
  ADD COLUMN operation ENUM('ADD','NET'),
  ADD COLUMN modifier_amount INTEGER,
  ADD COLUMN attribute_modified ENUM('Card', 'Gold', 'Purchase');

CREATE TABLE Thunderstone (
  card_id INTEGER NOT NULL,
  victory_points INTEGER NOT NULL,
  PRIMARY KEY (card_id)
);
-- !Downs
ALTER TABLE DungeonEffect DROP COLUMN need_type, DROP COLUMN `repeatable`, DROP COLUMN operation,
    DROP COLUMN modifier_amount, DROP COLUMN attribute_modified;
ALTER TABLE BattleEffect DROP COLUMN need_type, DROP COLUMN `repeatable`, DROP COLUMN operation,
    DROP COLUMN modifier_amount, DROP COLUMN attribute_modified;
ALTER TABLE VillageEffect DROP COLUMN need_type, DROP COLUMN `repeatable`, DROP COLUMN operation,
    DROP COLUMN modifier_amount, DROP COLUMN attribute_modified;
DROP TABLE Thunderstone

