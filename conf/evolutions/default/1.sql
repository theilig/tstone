-- !Ups
CREATE TABLE Game (
    game_id INTEGER PRIMARY KEY AUTO_INCREMENT,
    state TEXT NOT NULL,
    completed BOOLEAN NOT NULL DEFAULT 0,
    KEY completed (completed)
);

CREATE TABLE GamePlayers (
    game_player_id INTEGER PRIMARY KEY AUTO_INCREMENT,
    game_id INTEGER NOT NULL,
    user_id INTEGER NOT NULL,
    UNIQUE KEY game_player(game_id, user_id)
);

CREATE TABLE User (
    user_id INTEGER PRIMARY KEY AUTO_INCREMENT,
    first_name VARCHAR(200) NOT NULL,
    last_name VARCHAR(200) NOT NULL,
    password_hash VARCHAR(200) NOT NULL,
    email VARCHAR(256) NOT NULL,
    confirmed BOOLEAN NOT NULL DEFAULT 0,
    UNIQUE KEY user_email (email)
);

CREATE TABLE UserConfirmation (
    user_confirmation_id INTEGER PRIMARY KEY AUTO_INCREMENT,
    token VARCHAR(256) NOT NULL,
    user_id INTEGER NOT NULL,
    UNIQUE KEY confirmation_user (user_id),
    UNIQUE KEY confirmation_token (token)
);

CREATE TABLE Tokens (
    token_id INTEGER PRIMARY KEY AUTO_INCREMENT,
    expires INTEGER NOT NULL,
    user_id INTEGER NOT NULL,
    token_value VARCHAR(256) NOT NULL,
    UNIQUE KEY token_value (token_value),
    UNIQUE KEY user_id (user_id)
);

CREATE TABLE Card (
    card_id INTEGER PRIMARY KEY AUTO_INCREMENT,
    image VARCHAR(200) NOT NULL
);

CREATE TABLE ItemTrait (
    trait_id INTEGER PRIMARY KEY AUTO_INCREMENT,
    card_id INTEGER NOT NULL,
    trait VARCHAR(100) NOT NULL,
    KEY card_id (card_id)
);

CREATE TABLE HeroClass (
    trait_id INTEGER PRIMARY KEY AUTO_INCREMENT,
    card_id INTEGER NOT NULL,
    trait VARCHAR(100) NOT NULL,
    KEY card_id (card_id)
);

CREATE TABLE MonsterType (
    trait_id INTEGER PRIMARY KEY AUTO_INCREMENT,
    card_id INTEGER NOT NULL,
    trait VARCHAR(100) NOT NULL,
    KEY card_id (card_id)
);

CREATE TABLE BattleEffect (
    effect_id INTEGER PRIMARY KEY AUTO_INCREMENT,
    card_id INTEGER NOT NULL,
    effect VARCHAR(200) NOT NULL,
    KEY card_id (card_id)
);

CREATE TABLE DungeonEffect (
    effect_id INTEGER PRIMARY KEY AUTO_INCREMENT,
    card_id INTEGER NOT NULL,
    effect VARCHAR(200) NOT NULL,
    KEY card_id (card_id)
);

CREATE TABLE VillageEffect (
    effect_id INTEGER PRIMARY KEY AUTO_INCREMENT,
    card_id INTEGER NOT NULL,
    effect VARCHAR(200) NOT NULL,
    KEY card_id (card_id)
);

CREATE TABLE BreachEffect (
    effect_id INTEGER PRIMARY KEY AUTO_INCREMENT,
    card_id INTEGER NOT NULL,
    effect VARCHAR(200) NOT NULL,
    KEY card_id (card_id)
);

CREATE TABLE Item (
    card_id INTEGER PRIMARY KEY,
    name VARCHAR(100) NOT NULL,
    light INTEGER NOT NULL,
    weight INTEGER,
    cost INTEGER NOT NULL,
    gold_value INTEGER NOT NULL,
    victory_points INTEGER NOT NULL
);

CREATE TABLE Hero (
    card_id INTEGER PRIMARY KEY,
    name VARCHAR(100) NOT NULL,
    light INTEGER NOT NULL,
    strength INTEGER NOT NULL,
    level INTEGER NOT NULL,
    cost INTEGER NOT NULL,
    gold_value INTEGER NOT NULL,
    victory_points INTEGER NOT NULL
);

CREATE TABLE Monster (
    card_id INTEGER PRIMARY KEY,
    name VaRCHAR(100) NOT NULL,
    light INTEGER NOT NULL,
    health INTEGER NOT NULL,
    gold_value INTEGER NOT NULL,
    victory_points INTEGER NOT NULL
);


-- !Downs
DROP TABLE Game;
DROP TABLE User;
DROP TABLE UserConfirmation;
DROP TABLE Tokens;
DROP TABLE VillageEffect;
DROP TABLE DungeonEffect;
DROP TABLE BattleEffect;
DROP TABLE BreachEffect;
DROP TABLE Item;
DROP TABLE Hero;
DROP TABLE Monster;
DROP TABLE Card;
DROP TABLE MonsterType;
DROP TABLE ItemTrait;
DROP TABLE HeroClass;
