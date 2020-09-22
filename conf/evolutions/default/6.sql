# noinspection SqlResolveForFile
-- !Ups
ALTER TABLE Card ADD COLUMN frequency INTEGER;
DROP TABLE HeroClass;
ALTER TABLE Hero ADD COLUMN hero_classes VARCHAR(100) NOT NULL;
ALTER TABLE Item ADD COLUMN item_traits VARCHAR(100) NOT NULL;
DROP TABLE ItemTrait;
ALTER TABLE Monster ADD COLUMN monster_types VARCHAR(100) NOT NULL;
DROP TABLE MonsterType;
ALTER TABLE Spell ADD COLUMN spell_types VARCHAR(100) NOT NULL;
ALTER TABLE Villager ADD COLUMN villager_types VARCHAR(100) NOT NULL;
ALTER TABLE Weapon ADD COLUMN weapon_types VARCHAR(100) NOT NULL;

-- !Downs
ALTER TABLE Card DROP COLUMN frequency;
ALTER TABLE Hero DROP COLUMN hero_classes;
CREATE TABLE HeroClass (bogus INTEGER);
ALTER TABLE Item DROP COLUMN item_traits;
CREATE TABLE ItemTrait (bogus INTEGER);
ALTER TABLE Monster DROP COLUMN monster_types;
CREATE TABLE MonsterType (bogus INTEGER);
ALTER TABLE Spell DROP COLUMN spell_types;
ALTER TABLE Villager DROP COLUMN villager_types;
ALTER tABLE Weapon DROP COLUMN weapon_types;

