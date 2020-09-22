# noinspection SqlResolveForFile
-- !Ups
DROP TABLE BattleEffect;
DROP TABLE DungeonEffect;
DROP TABLE VillageEffect;
CREATE TABLE TurnEffect (
    effect_id int(11) NOT NULL AUTO_INCREMENT,
    `card_id` int(11) NOT NULL,
    effect_type ENUM('Village', 'Dungeon', 'Battle') NOT NULL,
    `need_type` varchar(100) DEFAULT NULL,
    repeatable tinyint(1) NOT NULL,
    operation enum('ADD','NET','SUBTRACT','DIVIDE','MULTIPLY') DEFAULT NULL,
    `modifier_amount` int(11) DEFAULT NULL,
    `attribute_modified` enum('Card','Gold','Purchase','Experience','ATT','MATT','Light','Strength','Weight') DEFAULT NULL,
      PRIMARY KEY (`effect_id`),
  KEY `card_id` (`card_id`)
);

-- !Downs
DROP TABLE TurnEffect;
CREATE TABLE `VillageEffect` (
  `effect_id` int(11) NOT NULL AUTO_INCREMENT,
  `card_id` int(11) NOT NULL,
  `effect` varchar(200) DEFAULT NULL,
  `need_type` varchar(100) DEFAULT NULL,
  `repeatable` tinyint(1) NOT NULL,
  `operation` enum('ADD','NET') DEFAULT NULL,
  `modifier_amount` int(11) DEFAULT NULL,
  `attribute_modified` enum('Card','Gold','Purchase','Experience') DEFAULT NULL,
  PRIMARY KEY (`effect_id`),
  PRIMARY KEY (`effect_id`),
);

CREATE TABLE `BattleEffect` (
  `effect_id` int(11) NOT NULL AUTO_INCREMENT,
  `card_id` int(11) NOT NULL,
  `effect` varchar(200) DEFAULT NULL,
  `need_type` varchar(100) DEFAULT NULL,
  `repeatable` tinyint(1) NOT NULL,
  `operation` enum('MULTIPLY','ADD','SUBTRACT','DIVIDE') DEFAULT NULL,
  `modifier_amount` int(11) DEFAULT NULL,
  `attribute_modified` enum('Card','ATT','MATT','Light','Strength') DEFAULT NULL,
  PRIMARY KEY (`effect_id`),
);

CREATE TABLE `DungeonEffect` (
  `effect_id` int(11) NOT NULL AUTO_INCREMENT,
  `card_id` int(11) NOT NULL,
  `effect` varchar(200) DEFAULT NULL,
  `need_type` varchar(100) DEFAULT NULL,
  `repeatable` tinyint(1) NOT NULL,
  `operation` enum('MULTIPLY','ADD','SUBTRACT','DIVIDE') DEFAULT NULL,
  `modifier_amount` int(11) DEFAULT NULL,
  `attribute_modified` enum('Card','ATT','MATT','Strength','Weight','Experience') DEFAULT NULL,
  PRIMARY KEY (`effect_id`),
  KEY `card_id` (`card_id`)
);