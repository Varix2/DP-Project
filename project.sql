
-- Crear la tabla "Utilizador"
CREATE TABLE Users (
    Email VARCHAR(50) PRIMARY KEY,
    Uname VARCHAR(50,
    Password VARCHAR(100),
    RoleId INT,
    FOREIGN KEY (RoleId) REFERENCES Roles(Id)
);

--  create table events
CREATE TABLE Events (
    Id INT PRIMARY KEY AUTO_INCREMENT,
    EventName VARCHAR(50),
    Location VARCHAR(100),
    DateOfCompletion DATE,
    StartDate VARCHAR(20),
    EndDate VARCHAR(20)
);

--create table attendance
CREATE TABLE Attendance(
    Id PRIMARY KEY AUTO_INCREMENT,
    EventId INT,
    UserId INT,
    CreatedAt VARCHAR(50),
    FOREIGN KEY (EventId) REFERENCES Events(Id),
    FOREIGN KEY (UserId) REFERENCES Users(Id)
);

--create table user roles
CREATE TABLE Roles(
    Id PRIMARY KEY AUTO_INCREMENT,
    Description VARCHAR(20)
);

