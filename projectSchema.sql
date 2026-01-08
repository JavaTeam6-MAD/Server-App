CREATE DATABASE IF NOT EXISTS GameSystem;
USE GameSystem;

-- 1. Create the Player table
CREATE TABLE Player (
    ID INT AUTO_INCREMENT PRIMARY KEY NOT NULL,
    user_name VARCHAR(50) NOT NULL UNIQUE,
    hashed_pass VARCHAR(255) NOT NULL,
    avatar VARCHAR(50) default "dragon",
    score INT DEFAULT 0,
    isActive BOOLEAN DEFAULT FALSE,
    isAvailable BOOLEAN DEFAULT FALSE
);

-- 2. Create the Game table
CREATE TABLE Game (
    ID INT AUTO_INCREMENT PRIMARY KEY NOT NULL,
    date DATETIME DEFAULT CURRENT_TIMESTAMP,
    status INT COMMENT '1: player1 won, 2: player2 won, 3: draw',
    player1ID INT NOT NULL,
    player2ID INT NOT NULL,
    -- Establishing Foreign Key relationships
    CONSTRAINT fk_player1 FOREIGN KEY (player1ID) REFERENCES Player(ID),
    CONSTRAINT fk_player2 FOREIGN KEY (player2ID) REFERENCES Player(ID)
);

-- 3. Insert Dummy Players for testing
INSERT INTO Player (user_name, hashed_pass, avatar, score, isActive, isAvailable) VALUES
('Alice', 'pass123', 'dragon', 150, FALSE, FALSE),
('Bob', 'pass456', 'knight', 120, FALSE, FALSE),
('Charlie', 'pass789', 'wizard', 90, FALSE, FALSE),
('Diana', 'pass321', 'archer', 200, FALSE, FALSE),
('Eve', 'pass654', 'warrior', 80, FALSE, FALSE);

-- 4. Insert Dummy Games for testing
-- Games for Alice (ID: 1)
INSERT INTO Game (date, status, player1ID, player2ID) VALUES
(DATE_SUB(NOW(), INTERVAL 5 DAY), 1, 1, 2),  -- Alice won against Bob
(DATE_SUB(NOW(), INTERVAL 4 DAY), 2, 1, 3),  -- Alice lost to Charlie
(DATE_SUB(NOW(), INTERVAL 3 DAY), 3, 1, 4),  -- Alice drew with Diana
(DATE_SUB(NOW(), INTERVAL 2 DAY), 1, 1, 5),  -- Alice won against Eve
(DATE_SUB(NOW(), INTERVAL 1 DAY), 1, 2, 1),  -- Bob won against Alice

-- Games for Bob (ID: 2)
(DATE_SUB(NOW(), INTERVAL 6 DAY), 1, 2, 3),  -- Bob won against Charlie
(DATE_SUB(NOW(), INTERVAL 4 DAY), 2, 2, 4),  -- Bob lost to Diana
(DATE_SUB(NOW(), INTERVAL 3 DAY), 3, 5, 2),  -- Eve drew with Bob

-- Games for Charlie (ID: 3)
(DATE_SUB(NOW(), INTERVAL 5 DAY), 1, 3, 4),  -- Charlie won against Diana
(DATE_SUB(NOW(), INTERVAL 2 DAY), 2, 5, 3),  -- Charlie lost to Eve

-- Games for Diana (ID: 4)
(DATE_SUB(NOW(), INTERVAL 7 DAY), 1, 4, 5),  -- Diana won against Eve
(DATE_SUB(NOW(), INTERVAL 1 DAY), 3, 4, 3),  -- Diana drew with Charlie

-- Games for Eve (ID: 5)
(DATE_SUB(NOW(), INTERVAL 6 DAY), 2, 1, 5);  -- Eve lost to Alice

