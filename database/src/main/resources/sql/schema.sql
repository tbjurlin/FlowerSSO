SET FOREIGN_KEY_CHECKS = 0;

DROP TABLE IF EXISTS Credentials;
DROP TABLE IF EXISTS Titles;
DROP TABLE IF EXISTS Departments;
DROP TABLE IF EXISTS Locations;
DROP TABLE IF EXISTS UserRoles;

SET FOREIGN_KEY_CHECKS = 1;

CREATE TABLE Credentials (
    id INT AUTO_INCREMENT,
    email VARCHAR(64),
    password VARCHAR(64),
    firstName VARCHAR(64),
    lastName VARCHAR(64),
    titleId INT,
    departmentId INT,
    locationId INT,
    userRoleId INT,

    PRIMARY KEY (id),
    FOREIGN KEY (titleId) REFERENCES Titles(id),
    FOREIGN KEY (departmentId) REFERENCES Departments(id),
    FOREIGN KEY (locationId) REFERENCES Locations(id),
    FOREIGN KEY (userRoleId) REFERENCES UserRoles(id)
);

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
    location VARCHAR(64)

    PRIMARY KEY (id)
);

CREATE TABLE UserRoles (
    id int AUTO_INCREMENT,
    userRole VARCHAR(64)

    PRIMARY KEY (id)
);


INSERT INTO Titles (userRole) VALUES (Aide);
INSERT INTO Titles (userRole) VALUES (Developer);
INSERT INTO Titles (userRole) VALUES (Sales Agent);
INSERT INTO Titles (userRole) VALUES (Manager);

INSERT INTO Departments (department) VALUES (Sales);
INSERT INTO Departments (department) VALUES (Information Technology);
INSERT INTO Departments (department) VALUES (Legal);
INSERT INTO Departments (department) VALUES (Hr);

INSERT INTO Locations (location) VALUES (United States);
INSERT INTO Locations (location) VALUES (Japan);
INSERT INTO Locations (location) VALUES (Brazil);
INSERT INTO Locations (location) VALUES (Germany);
INSERT INTO Locations (location) VALUES (South Africa);

INSERT INTO UserRoles (userRole) VALUES (Manager);
INSERT INTO UserRoles (userRole) VALUES (Contributor);
INSERT INTO UserRoles (userRole) VALUES (User);
INSERT INTO UserRoles (userRole) VALUES (Admin);

INSERT INTO Credentials (email, password) VALUES ('admin', 'admin');
