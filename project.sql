CREATE TABLE Users (
    id INTEGER PRIMARY KEY,
    name VARCHAR(50),
    email VARCHAR(50) UNIQUE,
    password VARCHAR(100),
    roleId INTEGER,
    FOREIGN KEY (roleId) REFERENCES Roles(id)
);

CREATE TABLE Events (
    id INTEGER PRIMARY KEY AUTOINCREMENT,
    name VARCHAR(50),
    location VARCHAR(100),
    date DATE,
    startTime DATETIME,
    endTime DATETIME
);

CREATE TABLE Attendance (
    id INTEGER PRIMARY KEY AUTOINCREMENT,
    idEvent INTEGER,
    idGuest VARCHAR(50),
    FOREIGN KEY (idEvent) REFERENCES Events(id),
    FOREIGN KEY (idGuest) REFERENCES Users(email)
);

CREATE TABLE Roles (
    id INTEGER PRIMARY KEY AUTOINCREMENT,
    roleName VARCHAR(30) UNIQUE
);

CREATE TABLE Version (
    id INTEGER PRIMARY KEY,
    versionNumber INTEGER
);


