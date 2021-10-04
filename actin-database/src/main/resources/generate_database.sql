SET FOREIGN_KEY_CHECKS = 0;

DROP TABLE IF EXISTS patient;
CREATE TABLE patient
(   id int NOT NULL AUTO_INCREMENT,
    sampleId varchar(20) UNIQUE,
    sex varchar(10) NOT NULL,
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
    hasSigAberrationLatestEcg BOOLEAN,
    ecgAberrationDescription varchar(50),
    PRIMARY KEY (id)
);

DROP TABLE IF EXISTS cancerRelatedComplication;
CREATE TABLE cancerRelatedComplication
(   id int NOT NULL AUTO_INCREMENT,
    sampleId varchar(50) NOT NULL,
    name varchar(50),
    PRIMARY KEY (id)
);

DROP TABLE IF EXISTS priorTumorTreatment;
CREATE TABLE priorTumorTreatment
(   id int NOT NULL AUTO_INCREMENT,
    sampleId varchar(50) NOT NULL,
    name varchar(50),
    year int,
    category varchar(50),
    isSystemic BOOLEAN,
    chemoType varchar(50),
    immunoType varchar(50),
    targetedType varchar(200),
    hormoneType varchar(50),
    stemCellTransType varchar(50),
    PRIMARY KEY (id)
);

DROP TABLE IF EXISTS priorSecondPrimary;
CREATE TABLE priorSecondPrimary
(   id int NOT NULL AUTO_INCREMENT,
    sampleId varchar(50) NOT NULL,
    tumorLocation varchar(50),
    tumorSubLocation varchar(50),
    tumorType varchar(50),
    tumorSubType varchar(50),
    doids varchar(50),
    year int,
    isSecondPrimaryActive BOOLEAN,
    diagnosedYear int,
    PRIMARY KEY (id)
);

DROP TABLE IF EXISTS priorOtherCondition;
CREATE TABLE priorOtherCondition
(   id int NOT NULL AUTO_INCREMENT,
    sampleId varchar(50) NOT NULL,
    name varchar(500),
    doids varchar(50),
    category varchar(50),
    PRIMARY KEY (id)
);

DROP TABLE IF EXISTS otherComplication;
CREATE TABLE otherComplication
(   id int NOT NULL AUTO_INCREMENT,
    sampleId varchar(50) NOT NULL,
    name varchar(50),
    doids varchar(50),
    specialty varchar(50),
    onsetDate DATE,
    category varchar(50),
    status varchar(50),
    PRIMARY KEY (id)
);

DROP TABLE IF EXISTS labValue;
CREATE TABLE labValue
(   id int NOT NULL AUTO_INCREMENT,
    sampleId varchar(50) NOT NULL,
    date DATE,
    code varchar(50),
    name varchar(50),
    value double precision,
    unit varchar(50),
    refLimitLow double precision,
    refLimitUp double precision,
    isOutsideRef BOOLEAN,
    alertLimitLow double precision,
    alertLimitUp double precision,
    isWithinAlert BOOLEAN,
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
    name varchar(50),
    category varchar(50),
    criticality varchar(50),
    PRIMARY KEY (id)
);

DROP TABLE IF EXISTS surgery;
CREATE TABLE surgery
(   id int NOT NULL AUTO_INCREMENT,
    sampleId varchar(50) NOT NULL,
    endDate DATE,
    PRIMARY KEY (id)
);

DROP TABLE IF EXISTS bloodPressure;
CREATE TABLE bloodPressure
(   id int NOT NULL AUTO_INCREMENT,
    sampleId varchar(50) NOT NULL,
    date DATE,
    category varchar(50),
    value double precision,
    unit varchar(50),
    PRIMARY KEY (id)
);

DROP TABLE IF EXISTS bloodTransfusion;
CREATE TABLE bloodTransfusion
(   id int NOT NULL AUTO_INCREMENT,
    sampleId varchar(50) NOT NULL,
    date DATE,
    product varchar(50),
    PRIMARY KEY (id)
);

DROP TABLE IF EXISTS medication;
CREATE TABLE medication
(   id int NOT NULL AUTO_INCREMENT,
    sampleId varchar(50) NOT NULL,
    name varchar(50),
    type varchar(50),
    dosage double precision,
    unit varchar(50),
    frequencyUnit varchar(50),
    ifNeeded BOOLEAN,
    startDate DATE,
    stopDate DATE,
    PRIMARY KEY (id)
);

SET FOREIGN_KEY_CHECKS = 1;