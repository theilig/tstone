# noinspection SqlResolveForFile
-- !Ups
DROP TABLE GamePlayers;
-- !Downs
CREATE TABLE `GamePlayers` (
  `game_player_id` int(11) NOT NULL AUTO_INCREMENT,
  `game_id` int(11) NOT NULL,
  `user_id` int(11) NOT NULL,
  PRIMARY KEY (`game_player_id`),
  UNIQUE KEY `game_player` (`game_id`,`user_id`)
)
