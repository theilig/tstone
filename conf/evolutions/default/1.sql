-- !Ups
CREATE TABLE Game (
    game_id INTEGER PRIMARY KEY AUTO_INCREMENT,
);

CREATE TABLE User (
    user_id INTEGER PRIMARY KEY AUTO_INCREMENT,
    first_name VARCHAR(200) NOT NULL,
    last_name VARCHAR(200) NOT NULL,
    password_hash VARCHAR(200) NOT NULL,
    email VARCHAR(256) NOT NULL,
    confirmed BOOLEAN DEFAULT 0,
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


-- !Downs
DROP TABLE Game;
DROP TABLE User;
DROP TABLE UserConfirmation;
DROP TABLE Tokens;
