SET FOREIGN_KEY_CHECKS = 0;

DROP TABLE IF EXISTS Credentials;
DROP TABLE IF EXISTS Titles;
DROP TABLE IF EXISTS Departments;
DROP TABLE IF EXISTS Locations;

SET FOREIGN_KEY_CHECKS = 1;

CREATE TABLE Titles (
    id INT AUTO_INCREMENT,
    title VARCHAR(64),

    PRIMARY KEY (id)
);

CREATE TABLE Departments (
    id INT AUTO_INCREMENT,
    department VARCHAR(64),

    PRIMARY KEY (id)
);

CREATE TABLE Locations (
    id INT AUTO_INCREMENT,
    location VARCHAR(64),

    PRIMARY KEY (id)
);

CREATE TABLE Credentials (
    id INT AUTO_INCREMENT,
    email VARCHAR(64),
    password VARCHAR(64),
    tempPassword VARCHAR(64) DEFAULT NULL,
    isAdmin BOOLEAN DEFAULT FALSE,
    firstName VARCHAR(64),
    lastName VARCHAR(64),
    titleId INT,
    departmentId INT,
    locationId INT,

    PRIMARY KEY (id),
    FOREIGN KEY (titleId) REFERENCES Titles(id) ON DELETE CASCADE,
    FOREIGN KEY (departmentId) REFERENCES Departments(id) ON DELETE CASCADE,
    FOREIGN KEY (locationId) REFERENCES Locations(id) ON DELETE CASCADE
);

INSERT INTO Titles (title) VALUES ("Aide");
INSERT INTO Titles (title) VALUES ("Developer");
INSERT INTO Titles (title) VALUES ("Sales Agent");
INSERT INTO Titles (title) VALUES ("Manager");

INSERT INTO Departments (department) VALUES ("Sales");
INSERT INTO Departments (department) VALUES ("Information Technology");
INSERT INTO Departments (department) VALUES ("Legal");
INSERT INTO Departments (department) VALUES ("Hr");

INSERT INTO Locations (location) VALUES ("United States");
INSERT INTO Locations (location) VALUES ("Japan");
INSERT INTO Locations (location) VALUES ("Brazil");
INSERT INTO Locations (location) VALUES ("Germany");
INSERT INTO Locations (location) VALUES ("South Africa");

INSERT INTO Credentials (email, password, isAdmin, firstName, lastName, titleId, departmentId, locationId) 
VALUES ('admin@flowersso.com', 'password9876', TRUE, "Georgette", "Fleugenheim", 
(SELECT id FROM Titles WHERE title="Manager"), (SELECT id FROM Departments WHERE department="Hr"), 
(SELECT id FROM Locations WHERE location="United States"));
