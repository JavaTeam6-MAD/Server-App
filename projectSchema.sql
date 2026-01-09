CREATE DATABASE IF NOT EXISTS GameSystem;
USE GameSystem;

-- 1. Create the Player table
CREATE TABLE Player (
    ID INT AUTO_INCREMENT PRIMARY KEY NOT NULL,
    user_name VARCHAR(50) NOT NULL UNIQUE,
    hashed_pass VARCHAR(255) NOT NULL,
    avatar VARCHAR(50) default "dragon",
    score BIGINT DEFAULT 0,
    isActive BOOLEAN DEFAULT FALSE,
    isAvailable BOOLEAN DEFAULT FALSE
);

-- 2. Create the Game table
CREATE TABLE Game (
    ID INT AUTO_INCREMENT PRIMARY KEY NOT NULL,
    date DATETIME DEFAULT CURRENT_TIMESTAMP,
    status INT COMMENT '1: player1 won, 2: player2 won, 3: draw',
    score BIGINT DEFAULT 0,
    isRecorded BOOLEAN DEFAULT FALSE,
    player1ID INT NOT NULL,
    player2ID INT NOT NULL,
    -- Establishing Foreign Key relationships
    CONSTRAINT fk_player1 FOREIGN KEY (player1ID) REFERENCES Player(ID),
    CONSTRAINT fk_player2 FOREIGN KEY (player2ID) REFERENCES Player(ID)
);
