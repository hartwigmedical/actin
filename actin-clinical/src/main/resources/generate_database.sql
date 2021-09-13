SET FOREIGN_KEY_CHECKS = 0;

DROP TABLE IF EXISTS patient;
CREATE TABLE patient
(   id int NOT NULL AUTO_INCREMENT,
    sampleId varchar(50) UNIQUE,
    sex varchar(50),
    birthYear int,
    registrationDate DATE,
    questionnaireDate DATE,
    PRIMARY KEY (id)
);

DROP TABLE IF EXISTS historyTumorTreatment;
CREATE TABLE historyTumorTreatment
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
    isSecondPrimaryCurated BOOLEAN,
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
    hasMeasurableBiopsyRecist BOOLEAN,
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

DROP TABLE IF EXISTS laboratory;
CREATE TABLE laboratory
(   id int NOT NULL AUTO_INCREMENT,
    sampleId varchar(50) UNIQUE,
    date DATE,
    valueCode varchar(50),
    valueName varchar(50),
    value double precision,
    valueUnit varchar(50),
    valueRefLow double precision,
    valueRefHigh double precision,
    isOutsideRef BOOLEAN,
    valueAlertLow double precision,
    valueAlertUp double precision,
    isWithinAlert BOOLEAN,
    PRIMARY KEY (id)
);

DROP TABLE IF EXISTS toxicity;
CREATE TABLE toxicity
(   id int NOT NULL AUTO_INCREMENT,
    sampleId varchar(50) UNIQUE,
    source varchar(50),
    evaluatedDate DATE,
    toxicity varchar(50),
    grade int,
    PRIMARY KEY (id)
);

DROP TABLE IF EXISTS allergy;
CREATE TABLE allergy
(   id int NOT NULL AUTO_INCREMENT,
    sampleId varchar(50) UNIQUE,
    allergy varchar(50),
    category varchar(50),
    criticality varchar(50),
    PRIMARY KEY (id)
);

DROP TABLE IF EXISTS surgery;
CREATE TABLE surgery
(   id int NOT NULL AUTO_INCREMENT,
    sampleId varchar(50) UNIQUE,
    surgeryEndDate DATE,
    PRIMARY KEY (id)
);

DROP TABLE IF EXISTS bloodPressure;
CREATE TABLE bloodPressure
(   id int NOT NULL AUTO_INCREMENT,
    sampleId varchar(50) UNIQUE,
    date DATE,
    category varchar(50),
    value double precision,
    valueQuantity varchar(50),
    PRIMARY KEY (id)
);

DROP TABLE IF EXISTS bloodTransfusion;
CREATE TABLE bloodTransfusion
(   id int NOT NULL AUTO_INCREMENT,
    sampleId varchar(50) UNIQUE,
    date DATE,
    bloodProduct varchar(50),
    PRIMARY KEY (id)
);

SET FOREIGN_KEY_CHECKS = 1;