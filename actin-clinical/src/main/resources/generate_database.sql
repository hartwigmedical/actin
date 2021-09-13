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

DROP TABLE IF EXISTS historyTumor;
CREATE TABLE historyTumor
(   id int NOT NULL AUTO_INCREMENT,
    sampleId varchar(50) UNIQUE,
    name varchar(50),
    year int,
    isSystemic BOOLEAN,
    isChemotherapy BOOLEAN,
    isImmunotherapy BOOLEAN,
    immunotherapyType varchar(50),
    isTargetedTherapy BOOLEAN,
    isHormoneTherapy BOOLEAN,
    isStemCellTransplant BOOLEAN,
    isRadiotherapy BOOLEAN,
    isSurgery BOOLEAN,
    PRIMARY KEY (id)
);

DROP TABLE IF EXISTS historySecondPrimary;
CREATE TABLE historySecondPrimary
(   id int NOT NULL AUTO_INCREMENT,
    sampleId varchar(50) UNIQUE,
    tumorLocation varchar(50),
    tumorSubLocation varchar(50),
    tumorType varchar(50),
    tumorSubType varchar(50),
    doid varchar(50),
    year int,
    secondPrimaryCurated BOOLEAN,
    curedDate DATE,
    PRIMARY KEY (id)
);

DROP TABLE IF EXISTS historyRelevantOther;
CREATE TABLE historyRelevantOther
(   id int NOT NULL AUTO_INCREMENT,
    sampleId varchar(50) UNIQUE,
    name varchar(500),
    nameDoid varchar(50),
    category varchar(50),
    categoryDoid varchar(50),
    PRIMARY KEY (id)
);

DROP TABLE IF EXISTS currentStatus;
CREATE TABLE currentStatus
(   id int NOT NULL AUTO_INCREMENT,
    sampleId varchar(50) UNIQUE,
    who int,
    hasCurrentInfection BOOLEAN,
    infectionDescription varchar(50),
    hasSigAberrationLatestEcg BOOLEAN,
    ecgAberrationDescription varchar(50),
    PRIMARY KEY (id)
);


DROP TABLE IF EXISTS currentCancerComplication;
CREATE TABLE currentCancerComplication
(   id int NOT NULL AUTO_INCREMENT,
    sampleId varchar(50) UNIQUE,
    cancerRelatedComplication varchar(50),
    PRIMARY KEY (id)
);

DROP TABLE IF EXISTS tumor;
CREATE TABLE tumor
(   id int NOT NULL AUTO_INCREMENT,
    sampleId varchar(50) UNIQUE,
    primaryTumorLocation varchar(50),
    primaryTumorSubLocation varchar(50),
    primaryTumorType varchar(50),
    primaryTumorSubType varchar(50),
    doid varchar(50),
    stage varchar(50),
    hasBrainLesions BOOLEAN,
    hasActiveBrainLesions BOOLEAN,
    hasSymptomaticBrainLesions BOOLEAN,
    hasCnsLesions BOOLEAN,
    hasActiveCnsLesions BOOLEAN,
    hasSymptomaticCnsLesions BOOLEAN,
    hasBoneLesions BOOLEAN,
    hasLiverLesions BOOLEAN,
    hasOtherLesions BOOLEAN,
    otherLesions varchar(50),
    biopsyMeasurableRecist BOOLEAN,
    PRIMARY KEY (id)
);

DROP TABLE IF EXISTS complication;
CREATE TABLE complication
(   id int NOT NULL AUTO_INCREMENT,
    sampleId varchar(50) UNIQUE,
    complication varchar(50),
    doid varchar(50),
    specialty varchar(50),
    onsetDate DATE,
    category varchar(50),
    status varchar(50),
    PRIMARY KEY (id)
);


SET FOREIGN_KEY_CHECKS = 1;