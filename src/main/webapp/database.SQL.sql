
CREATE DATABASE IF NOT EXISTS ConnorsDataBase2;
USE ConnorsDataBase2;

CREATE TABLE IF NOT EXISTS userInfo (
    username VARCHAR(255) NOT NULL UNIQUE,
    userPassword VARCHAR(255) NOT NULL,
    userEmail VARCHAR(255) NOT NULL UNIQUE,
    balance INT DEFAULT 0,
    TSLA INT DEFAULT 0,
    AAPL INT DEFAULT 0,
    AMD INT DEFAULT 0,
    NVDA INT DEFAULT 0,
    COIN INT DEFAULT 0,
    ROKU INT DEFAULT 0,
    RBLX INT DEFAULT 0,
    PRIMARY KEY (username)
);
