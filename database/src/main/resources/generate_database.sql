SET FOREIGN_KEY_CHECKS = 0;

-- CLINICAL
DROP TABLE IF EXISTS `patient`;
CREATE TABLE `patient`
(   `id` int NOT NULL AUTO_INCREMENT,
    `patientId` varchar(20) UNIQUE,
    `gender` varchar(10) NOT NULL,
    `birthYear` int NOT NULL,
    `registrationDate` DATE NOT NULL,
    `questionnaireDate` DATE,
    `otherMolecularPatientId` varchar(20),
    PRIMARY KEY (`id`)
);

DROP TABLE IF EXISTS `tumor`;
CREATE TABLE `tumor`
(   `id` int NOT NULL AUTO_INCREMENT,
    `patientId` varchar(50) UNIQUE,
    `primaryTumorLocation` varchar(100),
    `primaryTumorSubLocation` varchar(50),
    `primaryTumorType` varchar(50),
    `primaryTumorSubType` varchar(50),
    `primaryTumorExtraDetails` varchar(100),
    `doids` varchar(50),
    `stage` varchar(50),
    `hasMeasurableDisease` BOOLEAN,
    `hasBrainLesions` BOOLEAN,
    `hasActiveBrainLesions` BOOLEAN,
    `hasCnsLesions` BOOLEAN,
    `hasActiveCnsLesions` BOOLEAN,
    `hasBoneLesions` BOOLEAN,
    `hasLiverLesions` BOOLEAN,
    `hasLungLesions` BOOLEAN,
    `hasLymphNodeLesions` BOOLEAN,
    `otherLesions` varchar(500),
    `biopsyLocation` varchar(100),
    PRIMARY KEY (`id`)
);

DROP TABLE IF EXISTS `clinicalStatus`;
CREATE TABLE `clinicalStatus`
(   `id` int NOT NULL AUTO_INCREMENT,
    `patientId` varchar(50) UNIQUE,
    `who` int,
    `hasActiveInfection` BOOLEAN,
    `activeInfectionDescription` varchar(50),
    `hasToxicitiesGrade2` BOOLEAN,
    `hasSigAberrationLatestECG` BOOLEAN,
    `ecgAberrationDescription` varchar(50),
    `qtcfValue` int,
    `qtcfUnit` varchar(50),
    `jtcValue` int,
    `jtcUnit` varchar(50),
    `lvef` double precision,
    `hasComplications` BOOLEAN,
    PRIMARY KEY (`id`)
);

DROP TABLE IF EXISTS `treatmentHistoryEntry`;
CREATE TABLE `treatmentHistoryEntry`
(   `id` int NOT NULL AUTO_INCREMENT,
    `patientId` varchar(50) NOT NULL,
    `startYear` int,
    `startMonth` int,
    `name` varchar(100) NOT NULL,
    `synonyms` varchar(100) NOT NULL,
    `categories` varchar(100) NOT NULL,
    `drugs` varchar(500),
    `isSystemic` BOOLEAN NOT NULL,
    `isTrial` BOOLEAN,
    `trialAcronym` varchar(100),
    `intents` varchar(100),
    `maxCycles` int,
    `isInternal` BOOLEAN,
    `radioType` varchar(100),
    `stopYear` int,
    `stopMonth` int,
    `ongoingAsOf` date,
    `cycles` int,
    `bestResponse` varchar(50),
    `stopReason` varchar(50),
    `stopReasonDetail` varchar(200),
    `toxicities` varchar(200),
    PRIMARY KEY (`id`)
);

-- TODO: remove after table is cleaned up:
DROP TABLE IF EXISTS `priorTumorTreatment`;

DROP TABLE IF EXISTS `priorSecondPrimary`;
CREATE TABLE `priorSecondPrimary`
(   `id` int NOT NULL AUTO_INCREMENT,
    `patientId` varchar(50) NOT NULL,
    `tumorLocation` varchar(50) NOT NULL,
    `tumorSubLocation` varchar(50) NOT NULL,
    `tumorType` varchar(50) NOT NULL,
    `tumorSubType` varchar(50) NOT NULL,
    `doids` varchar(50) NOT NULL,
    `diagnosedYear` int,
    `diagnosedMonth` int,
    `treatmentHistory` varchar(150) NOT NULL,
    `lastTreatmentYear` int,
    `lastTreatmentMonth` int,
    `status` varchar(50) NOT NULL,
    PRIMARY KEY (`id`)
);

DROP TABLE IF EXISTS `priorOtherCondition`;
CREATE TABLE `priorOtherCondition`
(   `id` int NOT NULL AUTO_INCREMENT,
    `patientId` varchar(50) NOT NULL,
    `name` varchar(500) NOT NULL,
    `year` int,
    `month` int,
    `doids` varchar(50) NOT NULL,
    `category` varchar(100) NOT NULL,
    `isContraindicationForTherapy` BOOLEAN NOT NULL,
    PRIMARY KEY (`id`)
);

DROP TABLE IF EXISTS `priorMolecularTest`;
CREATE TABLE `priorMolecularTest`
(   `id` int NOT NULL AUTO_INCREMENT,
    `patientId` varchar(50) NOT NULL,
    `test` varchar(50) NOT NULL,
    `item` varchar(50) NOT NULL,
    `measure` varchar(50),
    `scoreText` varchar(100),
    `scoreValuePrefix` varchar(50),
    `scoreValue` double precision,
    `scoreValueUnit` varchar(50),
    `impliesPotentialIndeterminateStatus` BOOLEAN NOT NULL,
    PRIMARY KEY (`id`)
);

DROP TABLE IF EXISTS `complication`;
CREATE TABLE `complication`
(   `id` int NOT NULL AUTO_INCREMENT,
    `patientId` varchar(50) NOT NULL,
    `name` varchar(150) NOT NULL,
    `categories` varchar(150) NOT NULL,
    `year` int,
    `month` int,
    PRIMARY KEY (`id`)
);

DROP TABLE IF EXISTS `labValue`;
CREATE TABLE `labValue`
(   `id` int NOT NULL AUTO_INCREMENT,
    `patientId` varchar(50) NOT NULL,
    `date` DATE NOT NULL,
    `code` varchar(50) NOT NULL,
    `name` varchar(50) NOT NULL,
    `comparator` varchar(50) NOT NULL,
    `value` double precision NOT NULL,
    `unit` varchar(50) NOT NULL,
    `refLimitLow` double precision,
    `refLimitUp` double precision,
    `isOutsideRef` BOOLEAN,
    PRIMARY KEY (`id`)
);

DROP TABLE IF EXISTS `toxicity`;
CREATE TABLE `toxicity`
(   `id` int NOT NULL AUTO_INCREMENT,
    `patientId` varchar(50) NOT NULL,
    `name` varchar(50) NOT NULL,
    `categories` varchar(100) NOT NULL,
    `evaluatedDate` DATE NOT NULL,
    `source` varchar(50) NOT NULL,
    `grade` int,
    PRIMARY KEY (`id`)
);

DROP TABLE IF EXISTS `intolerance`;
CREATE TABLE `intolerance`
(   `id` int NOT NULL AUTO_INCREMENT,
    `patientId` varchar(50) NOT NULL,
    `name` varchar(50) NOT NULL,
    `doids` varchar(50) NOT NULL,
    `category` varchar(50) NOT NULL,
    `subcategories` varchar(100) NOT NULL,
    `type` varchar(50) NOT NULL,
    `clinicalStatus` varchar(50) NOT NULL,
    `verificationStatus` varchar(50) NOT NULL,
    `criticality` varchar(50) NOT NULL,
    PRIMARY KEY (`id`)
);

DROP TABLE IF EXISTS `surgery`;
CREATE TABLE `surgery`
(   `id` int NOT NULL AUTO_INCREMENT,
    `patientId` varchar(50) NOT NULL,
    `endDate` DATE NOT NULL,
    `status` varchar(50) NOT NULL,
    PRIMARY KEY (`id`)
);

DROP TABLE IF EXISTS `bodyWeight`;
CREATE TABLE `bodyWeight`
(   `id` int NOT NULL AUTO_INCREMENT,
     `patientId` varchar(50) NOT NULL,
     `date` DATE NOT NULL,
     `value` double precision NOT NULL,
     `unit` varchar(50) NOT NULL,
     PRIMARY KEY (`id`)
);

DROP TABLE IF EXISTS `vitalFunction`;
CREATE TABLE `vitalFunction`
(   `id` int NOT NULL AUTO_INCREMENT,
    `patientId` varchar(50) NOT NULL,
    `date` DATE NOT NULL,
    `category` varchar(50) NOT NULL,
    `subcategory` varchar(150) NOT NULL,
    `value` double precision NOT NULL,
    `unit` varchar(50) NOT NULL,
    PRIMARY KEY (`id`)
);

DROP TABLE IF EXISTS `bloodTransfusion`;
CREATE TABLE `bloodTransfusion`
(   `id` int NOT NULL AUTO_INCREMENT,
    `patientId` varchar(50) NOT NULL,
    `date` DATE NOT NULL,
    `product` varchar(50) NOT NULL,
    PRIMARY KEY (`id`)
);

DROP TABLE IF EXISTS `medication`;
CREATE TABLE `medication`
(   `id` int NOT NULL AUTO_INCREMENT,
    `patientId` varchar(50) NOT NULL,
    `name` varchar(100) NOT NULL,
    `codeATC` varchar(50),
    `categories` varchar(100),
    `chemicalSubgroupATC` varchar(100),
    `pharmacologicalSubgroupATC` varchar(100),
    `therapeuticSubgroupATC` varchar(100),
    `anatomicalMainGroupATC` varchar(100),
    `status` varchar(50),
    `administrationRoute` varchar(50),
    `dosageMin` double precision,
    `dosageMax` double precision,
    `dosageUnit` varchar(50),
    `frequency` double precision,
    `frequencyUnit` varchar(50),
    `ifNeeded` BOOLEAN,
    `startDate` DATE,
    `stopDate` DATE,
    PRIMARY KEY (`id`)
);

-- MOLECULAR
DROP TABLE IF EXISTS `molecular`;
CREATE TABLE `molecular`
(   `id` int NOT NULL AUTO_INCREMENT,
    `patientId` varchar(50) NOT NULL,
    `sampleId` varchar(50) NOT NULL UNIQUE,
    `experimentType` varchar(50) NOT NULL,
    `refGenomeVersion` varchar(20) NOT NULL,
    `experimentDate` DATE,
    `evidenceSource` varchar(50) NOT NULL,
    `externalTrialSource` varchar(50) NOT NULL,
    `containsTumorCells` BOOLEAN NOT NULL,
    `hasSufficientQuality` BOOLEAN NOT NULL,
    `purity` double precision,
    `ploidy` double precision,
    `predictedTumorType` varchar(50),
    `predictedTumorLikelihood` double precision,
    `isMicrosatelliteUnstable` BOOLEAN,
    `isHomologousRepairDeficient` BOOLEAN,
    `tumorMutationalBurden` double precision,
    `hasHighTumorMutationalBurden` BOOLEAN,
    `tumorMutationalLoad` int,
    `hasHighTumorMutationalLoad` BOOLEAN,
    PRIMARY KEY (`id`)
);

DROP TABLE IF EXISTS `microsatelliteEvidence`;
CREATE TABLE `microsatelliteEvidence`
(   `id` int NOT NULL AUTO_INCREMENT,
    `molecularId` int NOT NULL,
    `treatment` varchar(500) NOT NULL,
    `type` varchar(50) NOT NULL,
    PRIMARY KEY (`id`)
);

DROP TABLE IF EXISTS `homologousRepairEvidence`;
CREATE TABLE `homologousRepairEvidence`
(   `id` int NOT NULL AUTO_INCREMENT,
    `molecularId` int NOT NULL,
    `treatment` varchar(500) NOT NULL,
    `type` varchar(50) NOT NULL,
    PRIMARY KEY (`id`)
);

DROP TABLE IF EXISTS `tumorMutationalBurdenEvidence`;
CREATE TABLE `tumorMutationalBurdenEvidence`
(   `id` int NOT NULL AUTO_INCREMENT,
    `molecularId` int NOT NULL,
    `treatment` varchar(500) NOT NULL,
    `type` varchar(50) NOT NULL,
    PRIMARY KEY (`id`)
);

DROP TABLE IF EXISTS `tumorMutationalLoadEvidence`;
CREATE TABLE `tumorMutationalLoadEvidence`
(   `id` int NOT NULL AUTO_INCREMENT,
    `molecularId` int NOT NULL,
    `treatment` varchar(500) NOT NULL,
    `type` varchar(50) NOT NULL,
    PRIMARY KEY (`id`)
);

DROP TABLE IF EXISTS `variant`;
CREATE TABLE `variant`
(   `id` int NOT NULL AUTO_INCREMENT,
    `sampleId` varchar(50) NOT NULL,
    `isReportable` BOOLEAN NOT NULL,
    `event` varchar(200) NOT NULL,
    `driverLikelihood` varchar(50),
    `gene` varchar(50) NOT NULL,
    `geneRole` varchar(50) NOT NULL,
    `proteinEffect` varchar(50) NOT NULL,
    `isAssociatedWithDrugResistance` BOOLEAN,
    `type` varchar(50) NOT NULL,
    `variantCopyNumber` double precision NOT NULL,
    `totalCopyNumber` double precision NOT NULL,
    `isBiallelic` BOOLEAN NOT NULL,
    `isHotspot` BOOLEAN NOT NULL,
    `clonalLikelihood` double precision NOT NULL,
    `phaseGroups` varchar(50),
    `canonicalTranscriptId` varchar(50) NOT NULL,
    `canonicalHgvsCodingImpact` varchar(150) NOT NULL,
    `canonicalHgvsProteinImpact` varchar(50) NOT NULL,
    `canonicalAffectedCodon` int,
    `canonicalAffectedExon` int,
    `canonicalIsSpliceRegion` BOOLEAN NOT NULL,
    `canonicalEffects` varchar(250) NOT NULL,
    `canonicalCodingEffect` varchar(50),
    PRIMARY KEY (`id`)
);

DROP TABLE IF EXISTS `variantEvidence`;
CREATE TABLE `variantEvidence`
(   `id` int NOT NULL AUTO_INCREMENT,
    `variantId` int NOT NULL,
    `treatment` varchar(500) NOT NULL,
    `type` varchar(50) NOT NULL,
    PRIMARY KEY (`id`)
);

DROP TABLE IF EXISTS `variantOtherImpact`;
CREATE TABLE `variantOtherImpact`
(   `id` int NOT NULL AUTO_INCREMENT,
    `variantId` int NOT NULL,
    `transcriptId` varchar(50) NOT NULL,
    `hgvsCodingImpact` varchar(150) NOT NULL,
    `hgvsProteinImpact` varchar(50) NOT NULL,
    `affectedCodon` int,
    `affectedExon` int,
    `isSpliceRegion` BOOLEAN NOT NULL,
    `effects` varchar(250) NOT NULL,
    `codingEffect` varchar(50),
    PRIMARY KEY (`id`)
);

DROP TABLE IF EXISTS `copyNumber`;
CREATE TABLE `copyNumber`
(   `id` int NOT NULL AUTO_INCREMENT,
    `sampleId` varchar(50) NOT NULL,
    `isReportable` BOOLEAN NOT NULL,
    `event` varchar(50) NOT NULL,
    `driverLikelihood` varchar(50),
    `gene` varchar(50) NOT NULL,
    `geneRole` varchar(50) NOT NULL,
    `proteinEffect` varchar(50) NOT NULL,
    `isAssociatedWithDrugResistance` BOOLEAN,
    `type` varchar(50) NOT NULL,
    `minCopies` int NOT NULL,
    `maxCopies` int NOT NULL,
    PRIMARY KEY (`id`)
);

DROP TABLE IF EXISTS `copyNumberEvidence`;
CREATE TABLE `copyNumberEvidence`
(   `id` int NOT NULL AUTO_INCREMENT,
    `copyNumberId` int NOT NULL,
    `treatment` varchar(500) NOT NULL,
    `type` varchar(50) NOT NULL,
    PRIMARY KEY (`id`)
);

DROP TABLE IF EXISTS `homozygousDisruption`;
CREATE TABLE `homozygousDisruption`
(   `id` int NOT NULL AUTO_INCREMENT,
    `sampleId` varchar(50) NOT NULL,
    `isReportable` BOOLEAN NOT NULL,
    `event` varchar(50) NOT NULL,
    `driverLikelihood` varchar(50),
    `gene` varchar(50) NOT NULL,
    `geneRole` varchar(50) NOT NULL,
    `proteinEffect` varchar(50) NOT NULL,
    `isAssociatedWithDrugResistance` BOOLEAN,
    PRIMARY KEY (`id`)
);

DROP TABLE IF EXISTS `homozygousDisruptionEvidence`;
CREATE TABLE `homozygousDisruptionEvidence`
(   `id` int NOT NULL AUTO_INCREMENT,
    `homozygousDisruptionId` int NOT NULL,
    `treatment` varchar(500) NOT NULL,
    `type` varchar(50) NOT NULL,
    PRIMARY KEY (`id`)
);

DROP TABLE IF EXISTS `disruption`;
CREATE TABLE `disruption`
(   `id` int NOT NULL AUTO_INCREMENT,
    `sampleId` varchar(50) NOT NULL,
    `isReportable` BOOLEAN NOT NULL,
    `event` varchar(50) NOT NULL,
    `driverLikelihood` varchar(50),
    `gene` varchar(50) NOT NULL,
    `geneRole` varchar(50) NOT NULL,
    `proteinEffect` varchar(50) NOT NULL,
    `isAssociatedWithDrugResistance` BOOLEAN,
    `type` varchar(50) NOT NULL,
    `junctionCopyNumber` double precision NOT NULL,
    `undisruptedCopyNumber` double precision NOT NULL,
    `regionType` varchar(50) NOT NULL,
    `codingContext` varchar(50) NOT NULL,
    `clusterGroup` int NOT NULL,
    PRIMARY KEY (`id`)
);

DROP TABLE IF EXISTS `disruptionEvidence`;
CREATE TABLE `disruptionEvidence`
(   `id` int NOT NULL AUTO_INCREMENT,
    `disruptionId` int NOT NULL,
    `treatment` varchar(500) NOT NULL,
    `type` varchar(50) NOT NULL,
    PRIMARY KEY (`id`)
);

DROP TABLE IF EXISTS `fusion`;
CREATE TABLE `fusion`
(   `id` int NOT NULL AUTO_INCREMENT,
    `sampleId` varchar(50) NOT NULL,
    `isReportable` BOOLEAN NOT NULL,
    `event` varchar(50) NOT NULL,
    `driverLikelihood` varchar(50),
    `geneStart` varchar(50) NOT NULL,
    `geneTranscriptStart` varchar(50) NOT NULL,
    `fusedExonUp` int NOT NULL,
    `geneEnd` varchar(50) NOT NULL,
    `geneTranscriptEnd` varchar(50) NOT NULL,
    `fusedExonDown` int NOT NULL,
    `driverType` varchar(50) NOT NULL,
    `proteinEffect` varchar(50) NOT NULL,
    `isAssociatedWithDrugResistance` BOOLEAN,
    PRIMARY KEY (`id`)
);

DROP TABLE IF EXISTS `fusionEvidence`;
CREATE TABLE `fusionEvidence`
(   `id` int NOT NULL AUTO_INCREMENT,
    `fusionId` int NOT NULL,
    `treatment` varchar(500) NOT NULL,
    `type` varchar(50) NOT NULL,
    PRIMARY KEY (`id`)
);

DROP TABLE IF EXISTS `virus`;
CREATE TABLE `virus`
(   `id` int NOT NULL AUTO_INCREMENT,
    `sampleId` varchar(50) NOT NULL,
    `isReportable` BOOLEAN NOT NULL,
    `event` varchar(100) NOT NULL,
    `driverLikelihood` varchar(50),
    `name` varchar(50) NOT NULL,
    `type` varchar(50) NOT NULL,
    `isReliable` BOOLEAN NOT NULL,
    `integrations` int NOT NULL,
    PRIMARY KEY (`id`)
);

DROP TABLE IF EXISTS `virusEvidence`;
CREATE TABLE `virusEvidence`
(   `id` int NOT NULL AUTO_INCREMENT,
    `virusId` int NOT NULL,
    `treatment` varchar(500) NOT NULL,
    `type` varchar(50) NOT NULL,
    PRIMARY KEY (`id`)
);

DROP TABLE IF EXISTS `hlaAllele`;
CREATE TABLE `hlaAllele`
(   `id` int NOT NULL AUTO_INCREMENT,
    `sampleId` varchar(50) NOT NULL,
    `isReliable` BOOLEAN NOT NULL,
    `name` varchar(50) NOT NULL,
    `tumorCopyNumber` double precision NOT NULL,
    `hasSomaticMutations` BOOLEAN NOT NULL,
    PRIMARY KEY (`id`)
);

DROP TABLE IF EXISTS `pharmaco`;
CREATE TABLE `pharmaco`
(   `id` int NOT NULL AUTO_INCREMENT,
    `sampleId` varchar(50) NOT NULL,
    `gene` varchar(50) NOT NULL,
    `haplotype` varchar(50) NOT NULL,
    `haplotypeFunction` varchar(50) NOT NULL,
    PRIMARY KEY (`id`)
);




-- TREATMENT
DROP TABLE IF EXISTS `trial`;
CREATE TABLE `trial`
(   `id` int NOT NULL AUTO_INCREMENT,
    `code` varchar(50) UNIQUE,
    `open` BOOLEAN NOT NULL,
    `acronym` varchar(50) NOT NULL,
    `title` varchar(2500) NOT NULL,
    PRIMARY KEY (`id`)
);

DROP TABLE IF EXISTS `cohort`;
CREATE TABLE `cohort`
(   `id` int NOT NULL AUTO_INCREMENT,
    `trialId` int NOT NULL,
    `code` varchar(50) NOT NULL,
    `evaluable` BOOLEAN NOT NULL,
    `open` BOOLEAN NOT NULL,
    `slotsAvailable` BOOLEAN NOT NULL,
    `blacklist` BOOLEAN NOT NULL,
    `description` varchar(500) NOT NULL,
    PRIMARY KEY (`id`)
);

DROP TABLE IF EXISTS `eligibility`;
CREATE TABLE `eligibility`
(   `id` int NOT NULL AUTO_INCREMENT,
    `trialId` int NOT NULL,
    `cohortId` int,
    `parentId` int,
    `rule` varchar(100)  NOT NULL,
    `parameters` varchar(100) NOT NULL,
    `display` varchar(5000) NOT NULL,
    PRIMARY KEY (`id`)
);

DROP TABLE IF EXISTS `reference`;
CREATE TABLE `reference`
(   `id` int NOT NULL AUTO_INCREMENT,
    `code` varchar(50) NOT NULL,
    `text` varchar(15000) NOT NULL,
    PRIMARY KEY (`id`)
);

DROP TABLE IF EXISTS `eligibilityReference`;
CREATE TABLE `eligibilityReference`
(   `eligibilityId` int NOT NULL,
    `referenceId` int NOT NULL,
    PRIMARY KEY (`eligibilityId`, `referenceId`)
);


-- ALGO
DROP TABLE IF EXISTS `treatmentMatch`;
CREATE TABLE `treatmentMatch`
(   `id` int NOT NULL AUTO_INCREMENT,
    `patientId` varchar(50) NOT NULL,
    `sampleId` varchar(50) NOT NULL,
    `referenceDate` DATE NOT NULL,
    `referenceDateIsLive` BOOLEAN NOT NULL,
    PRIMARY KEY (`id`)
);

DROP TABLE IF EXISTS `trialMatch`;
CREATE TABLE `trialMatch`
(   `id` int NOT NULL AUTO_INCREMENT,
    `treatmentMatchId` int NOT NULL,
    `code` varchar(50) NOT NULL,
    `open` BOOLEAN NOT NULL,
    `acronym` varchar(50) NOT NULL,
    `title` varchar(2500) NOT NULL,
    `isEligible` BOOLEAN NOT NULL,
    PRIMARY KEY (`id`)
);

DROP TABLE IF EXISTS `cohortMatch`;
CREATE TABLE `cohortMatch`
(   `id` int NOT NULL AUTO_INCREMENT,
    `trialMatchId` int NOT NULL,
    `code` varchar(50) NOT NULL,
    `evaluable` BOOLEAN NOT NULL,
    `open` BOOLEAN NOT NULL,
    `slotsAvailable` BOOLEAN NOT NULL,
    `blacklist` BOOLEAN NOT NULL,
    `description` varchar(500) NOT NULL,
    `isEligible` BOOLEAN NOT NULL,
    PRIMARY KEY (`id`)
);

DROP TABLE IF EXISTS `evaluation`;
CREATE TABLE `evaluation`
(   `id` int NOT NULL AUTO_INCREMENT,
    `trialMatchId` int NOT NULL,
    `cohortMatchId` int,
    `eligibility` varchar(5000) NOT NULL,
    `result` varchar(50) NOT NULL,
    `recoverable` BOOLEAN NOT NULL,
    `inclusionMolecularEvents` varchar(300) NOT NULL,
    `exclusionMolecularEvents` varchar(300) NOT NULL,
    `passSpecificMessages` varchar(1000) NOT NULL,
    `passGeneralMessages` varchar(1000) NOT NULL,
    `warnSpecificMessages` varchar(2000) NOT NULL,
    `warnGeneralMessages` varchar(1000) NOT NULL,
    `undeterminedSpecificMessages` varchar(1000) NOT NULL,
    `undeterminedGeneralMessages` varchar(1000) NOT NULL,
    `failSpecificMessages` varchar(2000) NOT NULL,
    `failGeneralMessages` varchar(1000) NOT NULL,
    PRIMARY KEY (`id`)
);

SET FOREIGN_KEY_CHECKS = 1;