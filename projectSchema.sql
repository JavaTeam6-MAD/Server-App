CREATE DATABASE IF NOT EXISTS GameSystem;
USE GameSystem;

-- 1. Create the Player table
CREATE TABLE Player (
    ID INT AUTO_INCREMENT PRIMARY KEY,
    user_name VARCHAR(50) NOT NULL UNIQUE,
    hashed_pass VARCHAR(255) NOT NULL,
    char_no VARCHAR(10),
    score INT DEFAULT 0,
    isActive BOOLEAN DEFAULT FALSE,
    isAvailable BOOLEAN DEFAULT FALSE
);

-- 2. Create the Game table
CREATE TABLE Game (
    ID INT AUTO_INCREMENT PRIMARY KEY,
    date DATETIME DEFAULT CURRENT_TIMESTAMP,
    status INT,
    player1ID INT,
    player2ID INT,
    -- Establishing Foreign Key relationships
    CONSTRAINT fk_player1 FOREIGN KEY (player1ID) REFERENCES Player(ID),
    CONSTRAINT fk_player2 FOREIGN KEY (player2ID) REFERENCES Player(ID)
);