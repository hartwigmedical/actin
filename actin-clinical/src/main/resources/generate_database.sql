SET FOREIGN_KEY_CHECKS = 0;

DROP TABLE IF EXISTS patient;
CREATE TABLE patient
(   id int NOT NULL AUTO_INCREMENT,
    sampleId varchar(50) UNIQUE,
    gender varchar(50),
    birthYear int,
    registrationDate DATE,
    questionnaireDate DATE,
    arrivalDate DATE,
    reportDate DATE,
    PRIMARY KEY (id)
);

SET FOREIGN_KEY_CHECKS = 1;