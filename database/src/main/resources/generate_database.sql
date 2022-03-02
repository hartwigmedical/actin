SET FOREIGN_KEY_CHECKS = 0;

-- CLINICAL
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
    qtcfValue int,
    qtcfUnit varchar(50),
    lvef double precision,
    PRIMARY KEY (id)
);

DROP TABLE IF EXISTS priorTumorTreatment;
CREATE TABLE priorTumorTreatment
(   id int NOT NULL AUTO_INCREMENT,
    sampleId varchar(50) NOT NULL,
    name varchar(100) NOT NULL,
    year int,
    month int,
    categories varchar(100) NOT NULL,
    isSystemic BOOLEAN NOT NULL,
    chemoType varchar(50),
    immunoType varchar(50),
    targetedType varchar(200),
    hormoneType varchar(50),
    radioType varchar(50),
    transplantType varchar(50),
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
    year int,
    doids varchar(50) NOT NULL,
    category varchar(50) NOT NULL,
    PRIMARY KEY (id)
);

DROP TABLE IF EXISTS priorMolecularTest;
CREATE TABLE priorMolecularTest
(   id int NOT NULL AUTO_INCREMENT,
    sampleId varchar(50) NOT NULL,
    test varchar(50) NOT NULL,
    item varchar(50) NOT NULL,
    measure varchar(50),
    scoreText varchar(50),
    scoreValue double precision,
    unit varchar(50),
    PRIMARY KEY (id)
);

DROP TABLE IF EXISTS cancerRelatedComplication;
CREATE TABLE cancerRelatedComplication
(   id int NOT NULL AUTO_INCREMENT,
    sampleId varchar(50) NOT NULL,
    name varchar(150) NOT NULL,
    year int,
    month int,
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
    doids varchar(50),
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

DROP TABLE IF EXISTS vitalFunction;
CREATE TABLE vitalFunction
(   id int NOT NULL AUTO_INCREMENT,
    sampleId varchar(50) NOT NULL,
    date DATE NOT NULL,
    category varchar(50) NOT NULL,
    subcategory varchar(150) NOT NULL,
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
    name varchar(100) NOT NULL,
    categories varchar(100),
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



-- MOLECULAR
DROP TABLE IF EXISTS molecular;
CREATE TABLE molecular
(   id int NOT NULL AUTO_INCREMENT,
    sampleId varchar(50) UNIQUE,
    experimentType varchar(50) NOT NULL,
    experimentDate DATE,
    hasReliableQuality BOOLEAN NOT NULL,
    isMicrosatelliteUnstable BOOLEAN,
    isHomologousRepairDeficient BOOLEAN,
    tumorMutationalBurden double precision,
    tumorMutationalLoad int,
    PRIMARY KEY (id)
);

DROP TABLE IF EXISTS mutation;
CREATE TABLE mutation
(   id int NOT NULL AUTO_INCREMENT,
    sampleId varchar(50) NOT NULL,
    gene varchar(50) NOT NULL,
    mutation varchar(100) NOT NULL,
    PRIMARY KEY (id)
);

DROP TABLE IF EXISTS activatedGene;
CREATE TABLE activatedGene
(   id int NOT NULL AUTO_INCREMENT,
    sampleId varchar(50) NOT NULL,
    gene varchar(50) NOT NULL,
    PRIMARY KEY (id)
);

DROP TABLE IF EXISTS inactivatedGene;
CREATE TABLE inactivatedGene
(   id int NOT NULL AUTO_INCREMENT,
    sampleId varchar(50) NOT NULL,
    gene varchar(50) NOT NULL,
    hasBeenDeleted BOOLEAN NOT NULL,
    PRIMARY KEY (id)
);

DROP TABLE IF EXISTS amplifiedGene;
CREATE TABLE amplifiedGene
(   id int NOT NULL AUTO_INCREMENT,
    sampleId varchar(50) NOT NULL,
    gene varchar(50) NOT NULL,
    PRIMARY KEY (id)
);

DROP TABLE IF EXISTS wildtypeGene;
CREATE TABLE wildtypeGene
(   id int NOT NULL AUTO_INCREMENT,
    sampleId varchar(50) NOT NULL,
    gene varchar(50) NOT NULL,
    PRIMARY KEY (id)
);

DROP TABLE IF EXISTS fusionGene;
CREATE TABLE fusionGene
(   id int NOT NULL AUTO_INCREMENT,
    sampleId varchar(50) NOT NULL,
    fiveGene varchar(50) NOT NULL,
    threeGene varchar(50) NOT NULL,
    PRIMARY KEY (id)
);

DROP TABLE IF EXISTS molecularEvidence;
CREATE TABLE molecularEvidence
(   id int NOT NULL AUTO_INCREMENT,
    sampleId varchar(50) NOT NULL,
    type varchar(50) NOT NULL,
    event varchar(50) NOT NULL,
    treatment varchar(500) NOT NULL,
    isResponsive BOOLEAN NOT NULL,
    source varchar(50) NOT NULL,
    PRIMARY KEY (id)
);

SET FOREIGN_KEY_CHECKS = 1;