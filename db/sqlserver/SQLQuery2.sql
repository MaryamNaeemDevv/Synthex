use SynthexDB;

CREATE TABLE Users (
    userID VARCHAR(50) PRIMARY KEY,
    username VARCHAR(100) NOT NULL,
    email VARCHAR(100),
    passwordHash VARCHAR(255)
);


CREATE TABLE Scores (
    scoreID INT IDENTITY PRIMARY KEY,
    userID VARCHAR(50) FOREIGN KEY REFERENCES Users(userID),
    score INT DEFAULT 0,
    lastUpdated DATETIME DEFAULT GETDATE(),
    syncedToCloud BIT DEFAULT 0  -- 0 = not synced, 1 = synced
);

CREATE TABLE Friends (
    userID VARCHAR(50),
    friendID VARCHAR(50),
    since DATETIME DEFAULT GETDATE(),
    PRIMARY KEY (userID, friendID)
);


INSERT INTO Users VALUES ('user_001', 'testuser', 'test@gmail.com', 'hash123');
INSERT INTO Users VALUES ('user_002', 'roboto', 'roboto@gmail.com', 'hash456');

INSERT INTO Scores (userID, score) VALUES ('user_001', 9500);
INSERT INTO Scores (userID, score) VALUES ('user_002', 4200);


