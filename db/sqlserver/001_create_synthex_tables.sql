-- ============================================================
-- Synthex SQL Server Schema
-- Database: SynthexDB
-- Run SQLQuery1.sql first:
--   CREATE DATABASE SynthexDB;
-- Then run this script.
-- ============================================================
CREATE DATABASE SynthexDB;


USE SynthexDB;
GO

-- 1. Users
IF NOT EXISTS (SELECT * FROM sys.tables WHERE name = 'Users')
BEGIN
    CREATE TABLE Users (
        userID       NVARCHAR(100) PRIMARY KEY,
        username     NVARCHAR(100) NOT NULL,
        email        NVARCHAR(200) NULL,
        passwordHash NVARCHAR(255) NULL,
        createdAt    DATETIME2 DEFAULT SYSDATETIME()
    );
END
GO

-- 2. Scores
IF NOT EXISTS (SELECT * FROM sys.tables WHERE name = 'Scores')
BEGIN
    CREATE TABLE Scores (
        scoreID       INT IDENTITY(1,1) PRIMARY KEY,
        userID        NVARCHAR(100) NOT NULL,
        score         INT NOT NULL DEFAULT 0,
        syncedToCloud BIT DEFAULT 0,
        createdAt     DATETIME2 DEFAULT SYSDATETIME(),

        CONSTRAINT FK_Scores_Users
            FOREIGN KEY (userID) REFERENCES Users(userID)
    );
END
GO

-- 3. Sessions
IF NOT EXISTS (SELECT * FROM sys.tables WHERE name = 'Sessions')
BEGIN
    CREATE TABLE Sessions (
        sessionID  NVARCHAR(100) PRIMARY KEY,
        userID     NVARCHAR(100) NOT NULL,
        totalScore INT DEFAULT 0,
        status     NVARCHAR(20) DEFAULT 'active',
        startedAt  DATETIME2 NOT NULL DEFAULT SYSDATETIME(),
        endedAt    DATETIME2 NULL,

        CONSTRAINT FK_Sessions_Users
            FOREIGN KEY (userID) REFERENCES Users(userID)
    );
END
GO

-- 4. Submissions
IF NOT EXISTS (SELECT * FROM sys.tables WHERE name = 'Submissions')
BEGIN
    CREATE TABLE Submissions (
        submissionID  NVARCHAR(100) PRIMARY KEY,
        challengeID   NVARCHAR(100) NOT NULL,
        userID        NVARCHAR(100) NOT NULL,
        submittedCode NVARCHAR(MAX) NOT NULL,
        resultType    NVARCHAR(50) NOT NULL,
        isCorrect     BIT DEFAULT 0,
        timeTaken     FLOAT DEFAULT 0,
        createdAt     DATETIME2 DEFAULT SYSDATETIME(),

        CONSTRAINT FK_Submissions_Users
            FOREIGN KEY (userID) REFERENCES Users(userID)
    );
END
GO

-- 5. Optional local Friends table
-- Firebase is still the main source for friend requests/friend list,
-- but this table is useful if you want local/offline caching later.
IF NOT EXISTS (SELECT * FROM sys.tables WHERE name = 'Friends')
BEGIN
    CREATE TABLE Friends (
        userID   NVARCHAR(100) NOT NULL,
        friendID NVARCHAR(100) NOT NULL,
        since    DATETIME2 DEFAULT SYSDATETIME(),

        CONSTRAINT PK_Friends PRIMARY KEY (userID, friendID),
        CONSTRAINT FK_Friends_User
            FOREIGN KEY (userID) REFERENCES Users(userID),
        CONSTRAINT FK_Friends_Friend
            FOREIGN KEY (friendID) REFERENCES Users(userID)
    );
END
GO

-- Indexes
IF NOT EXISTS (SELECT * FROM sys.indexes WHERE name = 'IX_Scores_UserID')
    CREATE INDEX IX_Scores_UserID ON Scores(userID);
GO

IF NOT EXISTS (SELECT * FROM sys.indexes WHERE name = 'IX_Scores_Unsynced')
    CREATE INDEX IX_Scores_Unsynced ON Scores(syncedToCloud) WHERE syncedToCloud = 0;
GO

IF NOT EXISTS (SELECT * FROM sys.indexes WHERE name = 'IX_Sessions_UserID')
    CREATE INDEX IX_Sessions_UserID ON Sessions(userID);
GO

IF NOT EXISTS (SELECT * FROM sys.indexes WHERE name = 'IX_Submissions_UserID')
    CREATE INDEX IX_Submissions_UserID ON Submissions(userID);
GO

-- Seed test users
IF NOT EXISTS (SELECT 1 FROM Users WHERE userID = 'user_001')
BEGIN
    INSERT INTO Users (userID, username, email, passwordHash)
    VALUES ('user_001', 'testuser', 'test@gmail.com', 'hash123');
END
GO

IF NOT EXISTS (SELECT 1 FROM Users WHERE userID = 'user_002')
BEGIN
    INSERT INTO Users (userID, username, email, passwordHash)
    VALUES ('user_002', 'roboto', 'roboto@gmail.com', 'hash456');
END
GO

-- Seed test scores
IF NOT EXISTS (SELECT 1 FROM Scores WHERE userID = 'user_001' AND score = 9500)
BEGIN
    INSERT INTO Scores (userID, score) VALUES ('user_001', 9500);
END
GO

IF NOT EXISTS (SELECT 1 FROM Scores WHERE userID = 'user_002' AND score = 4200)
BEGIN
    INSERT INTO Scores (userID, score) VALUES ('user_002', 4200);
END
GO

PRINT 'SynthexDB schema setup completed successfully.';
GO