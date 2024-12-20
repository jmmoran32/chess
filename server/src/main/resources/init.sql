CREATE DATABASE IF NOT EXISTS chess;

USE chess;

CREATE TABLE IF NOT EXISTS USER_DATA
   (ID INT NOT NULL AUTO_INCREMENT,
    USERNAME VARCHAR(255) NOT NULL,
    PASS_HASH VARCHAR(255) NOT NULL,
    EMAIL VARCHAR(255) NOT NULL,
    PRIMARY KEY ID,
    INDEX (USERNAME));

CREATE TABLE IF NOT EXISTS GAME_DATA
   (GAME_ID INT NOT NULL AUTO_INCREMENT,
    WHITE_USERNAME VARCHAR(255),
    BLACK_USERNAME VARCHAR(255),
    GAME_NAME VARCHAR(255) NOT NULL,
    GAME VARCHAR(255) NOT NULL
    PRIMARY KEY GAME_ID,
    INDEX (GAME_ID));

CREATE TABLE IF NOT EXISTS AUTH_DATA
   (ID INT NOT NULL AUTO_INCREMENT,
    AUTH_TOKEN VARCHAR(255) NOT NULL,
    USERNAME VARCHAR(255) NOT NULL,
    PRIMARY KEY ID,
    INDEX (AUTH_TOKEN));
