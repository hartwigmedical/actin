SET FOREIGN_KEY_CHECKS = 0;

DROP TABLE IF EXISTS patient;
CREATE TABLE patient
(   id int NOT NULL AUTO_INCREMENT,
    sampleId varchar(20) UNIQUE,
    gender varchar(10) NOT NULL,
    birthYear int NOT NULL,
    registrationDate DATE NOT NULL,
    questionnaireDate DATE,
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
    primaryTumorExtraDetails varchar(50),
    doids varchar(50),
    stage varchar(50),
    hasMeasurableLesionRecist BOOLEAN,
    hasBrainLesions BOOLEAN,
    hasActiveBrainLesions BOOLEAN,
    hasSymptomaticBrainLesions BOOLEAN,
    hasCnsLesions BOOLEAN,
    hasActiveCnsLesions BOOLEAN,
    hasSymptomaticCnsLesions BOOLEAN,
    hasBoneLesions BOOLEAN,
    hasLiverLesions BOOLEAN,
    hasOtherLesions BOOLEAN,
    otherLesions varchar(500),
    biopsyLocation varchar(50),
    PRIMARY KEY (id)
);

DROP TABLE IF EXISTS clinicalStatus;
CREATE TABLE clinicalStatus
(   id int NOT NULL AUTO_INCREMENT,
    sampleId varchar(50) UNIQUE,
    who int,
    hasActiveInfection BOOLEAN,
    activeInfectionDescription varchar(50),
    hasSigAberrationLatestECG BOOLEAN,
    ecgAberrationDescription varchar(50),
    lvef double precision,
    PRIMARY KEY (id)
);

DROP TABLE IF EXISTS priorTumorTreatment;
CREATE TABLE priorTumorTreatment
(   id int NOT NULL AUTO_INCREMENT,
    sampleId varchar(50) NOT NULL,
    name varchar(50) NOT NULL,
    year int,
    month int,
    categories varchar(50) NOT NULL,
    isSystemic BOOLEAN NOT NULL,
    chemoType varchar(50),
    immunoType varchar(50),
    targetedType varchar(200),
    hormoneType varchar(50),
    stemCellTransType varchar(50),
    supportiveType varchar(50),
    trialAcronym varchar(50),
    PRIMARY KEY (id)
);

DROP TABLE IF EXISTS priorSecondPrimary;
CREATE TABLE priorSecondPrimary
(   id int NOT NULL AUTO_INCREMENT,
    sampleId varchar(50) NOT NULL,
    tumorLocation varchar(50) NOT NULL,
    tumorSubLocation varchar(50) NOT NULL,
    tumorType varchar(50) NOT NULL,
    tumorSubType varchar(50) NOT NULL,
    doids varchar(50) NOT NULL,
    diagnosedYear int,
    diagnosedMonth int,
    treatmentHistory varchar(150) NOT NULL,
    isActive BOOLEAN NOT NULL,
    PRIMARY KEY (id)
);

DROP TABLE IF EXISTS priorOtherCondition;
CREATE TABLE priorOtherCondition
(   id int NOT NULL AUTO_INCREMENT,
    sampleId varchar(50) NOT NULL,
    name varchar(500) NOT NULL,
    doids varchar(50) NOT NULL,
    category varchar(50) NOT NULL,
    PRIMARY KEY (id)
);

DROP TABLE IF EXISTS cancerRelatedComplication;
CREATE TABLE cancerRelatedComplication
(   id int NOT NULL AUTO_INCREMENT,
    sampleId varchar(50) NOT NULL,
    name varchar(50) NOT NULL,
    PRIMARY KEY (id)
);

DROP TABLE IF EXISTS otherComplication;
CREATE TABLE otherComplication
(   id int NOT NULL AUTO_INCREMENT,
    sampleId varchar(50) NOT NULL,
    name varchar(50) NOT NULL,
    doids varchar(50) NOT NULL,
    specialty varchar(50) NOT NULL,
    onsetDate DATE  NOT NULL,
    category varchar(50) NOT NULL,
    status varchar(50) NOT NULL,
    PRIMARY KEY (id)
);

DROP TABLE IF EXISTS labValue;
CREATE TABLE labValue
(   id int NOT NULL AUTO_INCREMENT,
    sampleId varchar(50) NOT NULL,
    date DATE NOT NULL,
    code varchar(50) NOT NULL,
    name varchar(50) NOT NULL,
    comparator varchar(50) NOT NULL,
    value double precision NOT NULL,
    unit varchar(50) NOT NULL,
    refLimitLow double precision,
    refLimitUp double precision,
    isOutsideRef BOOLEAN,
    PRIMARY KEY (id)
);

DROP TABLE IF EXISTS toxicity;
CREATE TABLE toxicity
(   id int NOT NULL AUTO_INCREMENT,
    sampleId varchar(50) NOT NULL,
    name varchar(50) NOT NULL,
    evaluatedDate DATE NOT NULL,
    source varchar(50) NOT NULL,
    grade int,
    PRIMARY KEY (id)
);

DROP TABLE IF EXISTS allergy;
CREATE TABLE allergy
(   id int NOT NULL AUTO_INCREMENT,
    sampleId varchar(50) NOT NULL,
    name varchar(50) NOT NULL,
    category varchar(50) NOT NULL,
    clinicalStatus varchar(50) NOT NULL,
    verificationStatus varchar(50) NOT NULL,
    criticality varchar(50) NOT NULL,
    PRIMARY KEY (id)
);

DROP TABLE IF EXISTS surgery;
CREATE TABLE surgery
(   id int NOT NULL AUTO_INCREMENT,
    sampleId varchar(50) NOT NULL,
    endDate DATE NOT NULL,
    PRIMARY KEY (id)
);

DROP TABLE IF EXISTS bodyWeight;
CREATE TABLE bodyWeight
(   id int NOT NULL AUTO_INCREMENT,
     sampleId varchar(50) NOT NULL,
     date DATE NOT NULL,
     value double precision NOT NULL,
     unit varchar(50) NOT NULL,
     PRIMARY KEY (id)
);

DROP TABLE IF EXISTS bloodPressure;
CREATE TABLE bloodPressure
(   id int NOT NULL AUTO_INCREMENT,
    sampleId varchar(50) NOT NULL,
    date DATE NOT NULL,
    category varchar(150) NOT NULL,
    value double precision NOT NULL,
    unit varchar(50) NOT NULL,
    PRIMARY KEY (id)
);

DROP TABLE IF EXISTS bloodTransfusion;
CREATE TABLE bloodTransfusion
(   id int NOT NULL AUTO_INCREMENT,
    sampleId varchar(50) NOT NULL,
    date DATE NOT NULL,
    product varchar(50) NOT NULL,
    PRIMARY KEY (id)
);

DROP TABLE IF EXISTS medication;
CREATE TABLE medication
(   id int NOT NULL AUTO_INCREMENT,
    sampleId varchar(50) NOT NULL,
    name varchar(50) NOT NULL,
    type varchar(100),
    dosageMin double precision,
    dosageMax double precision,
    dosageUnit varchar(50),
    frequency double precision,
    frequencyUnit varchar(50),
    ifNeeded BOOLEAN,
    startDate DATE,
    stopDate DATE,
    active BOOLEAN,
    PRIMARY KEY (id)
);

SET FOREIGN_KEY_CHECKS = 1;