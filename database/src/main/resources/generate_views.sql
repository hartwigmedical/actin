CREATE OR REPLACE VIEW criteriaMapping
AS (
SELECT  trial.code AS trialId, acronym AS trialAcronym, cohort.code AS cohortId, description AS cohortDescription,
        reference.code AS criterionCode, text AS criterionText, display AS eligibilityRule
    FROM eligibilityReference
    INNER JOIN (SELECT DISTINCT id, trialId, cohortId, display FROM eligibility) AS a ON a.id = eligibilityReference.eligibilityId
    INNER JOIN reference ON eligibilityReference.referenceId = reference.Id
    INNER JOIN trial ON trial.id = a.trialId
    LEFT JOIN cohort ON cohort.id = a.cohortId
    ORDER BY trialId
);

CREATE OR REPLACE VIEW eligibleCohorts
AS (
SELECT DISTINCT patientId, trialId, trialAcronym, cohortDescription
    FROM trialEvaluation
    WHERE ((isEligibleTrial AND NOT trialHasCohorts AND trialOpen) OR (isEligibleTrial AND isEligibleCohort AND cohortOpen AND NOT cohortBlacklist))
);

CREATE OR REPLACE VIEW drivers
AS (
SELECT * FROM (
	SELECT sampleId, event, driverLikelihood
    FROM variant
    WHERE isReportable
		UNION
	SELECT sampleId, event, driverLikelihood
    FROM amplification
    WHERE isReportable
		UNION
	SELECT sampleId, event, driverLikelihood
    FROM loss
    WHERE isReportable
		UNION
	SELECT sampleId, event, driverLikelihood
    FROM homozygousDisruption
    WHERE isReportable
		UNION
	SELECT sampleId, event, driverLikelihood
    FROM disruption
    WHERE isReportable
		UNION
	SELECT sampleId, event, driverLikelihood
    FROM fusion
    WHERE isReportable
		UNION
	SELECT sampleId, event, driverLikelihood
    FROM virus
    WHERE isReportable)
    AS a
ORDER BY 1,2
);

CREATE OR REPLACE VIEW evidence
AS (
SELECT * FROM (
	SELECT molecular.sampleId, treatment, type, IF(isMicrosatelliteUnstable,"MSI","MSS") AS event, NULL AS driverLikelihood, NULL AS isReportableDriver
	FROM microsatelliteEvidence INNER JOIN molecular ON microsatelliteEvidence.molecularId = molecular.id
		UNION
	SELECT molecular.sampleId, treatment, type, IF(isHomologousRepairDeficient,"HRD","HRP") AS event, NULL AS driverLikelihood, NULL AS isReportableDriver
	FROM homologousRepairEvidence INNER JOIN molecular ON homologousRepairEvidence.molecularId = molecular.id
		UNION
	SELECT molecular.sampleId, treatment, type, concat("TMB ", round(tumorMutationalBurden,1)) AS event, NULL AS driverLikelihood, NULL AS isReportableDriver
	FROM tumorMutationalBurdenEvidence INNER JOIN molecular ON tumorMutationalBurdenEvidence.molecularId = molecular.id
		UNION
	SELECT molecular.sampleId, treatment, type, concat("TML ", tumorMutationalLoad) AS event, NULL AS driverLikelihood, NULL AS isReportableDriver
	FROM tumorMutationalLoadEvidence INNER JOIN molecular ON tumorMutationalLoadEvidence.molecularId = molecular.id
		UNION
	SELECT variant.sampleId, treatment, variantEvidence.type, event, driverLikelihood, isReportable AS isReportableDriver
	FROM variantEvidence INNER JOIN variant ON variantEvidence.variantId = variant.Id
		UNION
	SELECT amplification.sampleId, treatment, type, event, driverLikelihood, isReportable AS isReportableDriver
	FROM amplificationEvidence INNER JOIN amplification ON amplificationEvidence.amplificationId = amplification.Id
		UNION
	SELECT loss.sampleId, treatment, type, event, driverLikelihood, isReportable AS isReportableDriver
	FROM lossEvidence INNER JOIN loss ON lossEvidence.lossId = loss.Id
		UNION
	SELECT homozygousDisruption.sampleId, treatment, type, event, driverLikelihood, isReportable AS isReportableDriver
	FROM homozygousDisruptionEvidence INNER JOIN homozygousDisruption ON homozygousDisruptionEvidence.homozygousDisruptionId = homozygousDisruption.Id
		UNION
	SELECT disruption.sampleId, treatment, disruptionEvidence.type, event, driverLikelihood, isReportable AS isReportableDriver
	FROM disruptionEvidence INNER JOIN disruption ON disruptionEvidence.disruptionId = disruption.Id
		UNION
	SELECT fusion.sampleId, treatment, type, event, driverLikelihood, isReportable AS isReportableDriver
	FROM fusionEvidence INNER JOIN fusion ON fusionEvidence.fusionId = fusion.Id
		UNION
	SELECT virus.sampleId, treatment, virusEvidence.type, event, driverLikelihood, isReportable AS isReportableDriver
	FROM virusEvidence INNER JOIN virus ON virusEvidence.virusId = virus.Id)
    AS a
);

CREATE OR REPLACE VIEW molecularDetails
AS (
SELECT * FROM (
SELECT  x.sampleId, IF(isHotspot,"Mutation (Hotspot)",IF(isBiallelic,"Mutation (Biallelic VUS)", "Mutation (VUS)")) AS type, x.event, concat(round(variantCopyNumber,1),"/",round(totalCopyNumber,1), " copies") AS details, driverLikelihood,
		group_concat(DISTINCT et.treatment) AS externalTrials,
	    IF(group_concat(DISTINCT e.type) LIKE '%Approved%', "Approved", IF(group_concat(DISTINCT e.type) LIKE '%On-label%', "On-label experimental", IF(group_concat(DISTINCT e.type) LIKE '%Off-label%', "Off-label experimental", IF(group_concat(DISTINCT e.type) LIKE '%Pre-clinical%', "Pre-clinical", NULL)))) AS treatmentEvidenceResponsive,
		IF(group_concat(DISTINCT e.type) LIKE '%Known%', "Known", IF(group_concat(DISTINCT e.type) LIKE '%Suspect%', "Suspected", NULL)) AS treatmentEvidenceResistance
    FROM variant x
	LEFT JOIN variantEvidence e ON x.id=e.variantId
    LEFT JOIN (SELECT * FROM variantEvidence WHERE type='Trial') et ON x.id=et.variantId
    WHERE isReportable
	GROUP BY 1,3
UNION
SELECT  x.sampleId, IF(isPartial, "Amplification (partial)", "Amplification") AS e2ype, x.event, concat(minCopies," copies") AS details, driverLikelihood,
		group_concat(distinct et.treatment) AS externalTrials,
	    IF(group_concat(DISTINCT e.type) LIKE '%Approved%', "Approved", IF(group_concat(DISTINCT e.type) LIKE '%On-label%', "On-label experimental", IF(group_concat(DISTINCT e.type) LIKE '%Off-label%', "Off-label experimental", IF(group_concat(DISTINCT e.type) LIKE '%Pre-clinical%', "Pre-clinical", NULL)))) AS treatmentEvidenceResponsive,
		IF(group_concat(DISTINCT e.type) LIKE '%Known%', "Known", IF(group_concat(DISTINCT e.type) LIKE '%Suspect%', "Suspected", NULL)) AS treatmentEvidenceResistance
	FROM amplification x
	LEFT JOIN amplificationEvidence e ON x.id=e.amplificationId
	LEFT JOIN (SELECT * FROM amplificationEvidence WHERE type='Trial') et ON x.id=et.amplificationId
    WHERE isReportable
	GROUP BY 1,3
UNION
SELECT  x.sampleId, IF(isPartial, "Loss (partial)", "Loss"), x.event, NULL AS details, driverLikelihood,
		group_concat(DISTINCT et.treatment) AS externalTrials,
	    IF(group_concat(DISTINCT e.type) LIKE '%Approved%', "Approved", IF(group_concat(DISTINCT e.type) LIKE '%On-label%', "On-label experimental", IF(group_concat(DISTINCT e.type) LIKE '%Off-label%', "Off-label experimental", IF(group_concat(DISTINCT e.type) LIKE '%Pre-clinical%', "Pre-clinical", NULL)))) AS treatmentEvidenceResponsive,
		IF(group_concat(DISTINCT e.type) LIKE '%Known%', "Known", IF(group_concat(DISTINCT e.type) LIKE '%Suspect%', "Suspected", NULL)) AS treatmentEvidenceResistance
    FROM loss x
	LEFT JOIN lossEvidence e ON x.id=e.lossId
	LEFT JOIN (SELECT * FROM lossEvidence WHERE type='Trial') et ON x.id=et.lossId
    WHERE isReportable
	GROUP BY 1,3
UNION
SELECT  x.sampleId, "Homozygous disruption" AS e2ype, x.event, NULL AS details, driverLikelihood,
		group_concat(DISTINCT et.treatment) AS externalTrials,
	    IF(group_concat(DISTINCT e.type) LIKE '%Approved%', "Approved", IF(group_concat(DISTINCT e.type) LIKE '%On-label%', "On-label experimental", IF(group_concat(DISTINCT e.type) LIKE '%Off-label%', "Off-label experimental", IF(group_concat(DISTINCT e.type) LIKE '%Pre-clinical%', "Pre-clinical", NULL)))) AS treatmentEvidenceResponsive,
		IF(group_concat(DISTINCT e.type) LIKE '%Known%', "Known", IF(group_concat(DISTINCT e.type) LIKE '%Suspect%', "Suspected", NULL)) AS treatmentEvidenceResistance
	FROM homozygousDisruption x
	LEFT JOIN homozygousDisruptionEvidence e ON x.id=e.homozygousDisruptionId
	LEFT JOIN (SELECT * FROM homozygousDisruptionEvidence WHERE type='Trial') et ON x.id=et.homozygousDisruptionId
    WHERE isReportable
	GROUP BY 1,3
UNION
SELECT  x.sampleId, IF(driverType LIKE '%PROMISCUOUS%',"Promiscuous fusion",IF(driverType LIKE '%KNOWN%',"Known fusion", driverType)) AS type, x.event, concat("Exon ", fusedExonUp, " - Exon ", fusedExonDown) AS details, driverLikelihood,
		group_concat(DISTINCT et.treatment) AS externalTrials,
	    IF(group_concat(DISTINCT e.type) LIKE '%Approved%', "Approved", IF(group_concat(DISTINCT e.type) LIKE '%On-label%', "On-label experimental", IF(group_concat(DISTINCT e.type) LIKE '%Off-label%', "Off-label experimental", IF(group_concat(DISTINCT e.type) LIKE '%Pre-clinical%', "Pre-clinical", NULL)))) AS treatmentEvidenceResponsive,
		IF(group_concat(DISTINCT e.type) LIKE '%Known%', "Known", IF(group_concat(DISTINCT e.type) LIKE '%Suspect%', "Suspected", NULL)) AS treatmentEvidenceResistance
	FROM fusion x
	LEFT JOIN fusionEvidence e ON x.id=e.fusionId
	LEFT JOIN (SELECT * FROM fusionEvidence WHERE type='Trial') et ON x.id=et.fusionId
    WHERE isReportable
	GROUP BY 1,3
UNION
SELECT  x.sampleId,"Disruption" AS type, x.event, concat(round(junctionCopyNumber,1), " disr. / ", round(undisruptedCopyNumber,1), " undisr. copies"), driverLikelihood,
		group_concat(DISTINCT et.treatment) AS externalTrials,
	    IF(group_concat(DISTINCT e.type) LIKE '%Approved%', "Approved", IF(group_concat(DISTINCT e.type) LIKE '%On-label%', "On-label experimental", IF(group_concat(DISTINCT e.type) LIKE '%Off-label%', "Off-label experimental", IF(group_concat(DISTINCT e.type) LIKE '%Pre-clinical%', "Pre-clinical", NULL)))) AS treatmentEvidenceResponsive,
		IF(group_concat(DISTINCT e.type) LIKE '%Known%', "Known", IF(group_concat(DISTINCT e.type) LIKE '%Suspect%', "Suspected", NULL)) AS treatmentEvidenceResistance
	FROM disruption x
	LEFT JOIN disruptionEvidence e ON x.id=e.disruptionId
	LEFT JOIN (SELECT * FROM disruptionEvidence WHERE type='Trial') et ON x.id=et.disruptionId
    WHERE isReportable
	GROUP BY 1,3
UNION
SELECT  x.sampleId, "Virus" AS type, x.event, concat(name, ", ", integrations, " integrations") AS details, driverLikelihood,
		group_concat(DISTINCT et.treatment) AS externalTrials,
	    IF(group_concat(DISTINCT e.type) LIKE '%Approved%', "Approved", IF(group_concat(DISTINCT e.type) LIKE '%On-label%', "On-label experimental", IF(group_concat(DISTINCT e.type) LIKE '%Off-label%', "Off-label experimental", IF(group_concat(DISTINCT e.type) LIKE '%Pre-clinical%', "Pre-clinical", NULL)))) AS treatmentEvidenceResponsive,
		IF(group_concat(DISTINCT e.type) LIKE '%Known%', "Known", IF(group_concat(DISTINCT e.type) LIKE '%Suspect%', "Suspected", NULL)) AS treatmentEvidenceResistance
	FROM virus x
	LEFT JOIN virusEvidence e ON x.id=e.virusId
	LEFT JOIN (SELECT * FROM virusEvidence WHERE type='Trial') et ON x.id=et.virusId
    WHERE isReportable
	GROUP BY 1,3
ORDER BY 1,2,3)
AS a
);

CREATE OR REPLACE VIEW trialEvaluation
AS (
SELECT * FROM (
SELECT  referenceDate, referenceDateIsLive, patientId, trialMatch.code AS trialId, trial.acronym AS trialAcronym, trial.open AS trialOpen,
        IF(trial.id IN (SELECT trialId FROM cohort),1,0) AS trialHasCohorts, trialMatch.isEligible AS isEligibleTrial,
        cohortMatch.code AS cohortId, cohortMatch.description AS cohortDescription, cohortMatch.open AS cohortOpen,
        cohortMatch.slotsAvailable AS cohortSlotsAvailable, cohortMatch.blacklist AS cohortBlacklist, cohortMatch.isEligible AS isEligibleCohort,
        eligibility AS eligibilityRule, result, recoverable, passSpecificMessages, passGeneralMessages, warnSpecificMessages, warnGeneralMessages,
        undeterminedSpecificMessages, undeterminedGeneralMessages, failSpecificMessages, failGeneralMessages
    FROM evaluation
    INNER JOIN trialMatch ON trialMatch.id = evaluation.trialMatchId
    INNER JOIN treatmentMatch ON treatmentMatch.id = trialMatch.treatmentMatchId
    LEFT JOIN trial ON trial.code = trialMatch.code
    LEFT JOIN cohortMatch ON trialMatch.id = cohortMatch.trialMatchId AND cohortMatch.Id = evaluation.cohortMatchId
UNION
SELECT  DISTINCT referenceDate, referenceDateIsLive, patientId, trialMatch.code AS trialId, trial.acronym AS trialAcronym, trial.open AS trialOpen,
        IF(trial.id IN (SELECT trialId FROM cohort),1,0) AS trialHasCohorts, trialMatch.isEligible AS isEligibleTrial,
        cohortMatch.code AS cohortId, cohortMatch.description AS cohortDescription, cohortMatch.open AS cohortOpen,
        cohortMatch.slotsAvailable AS cohortSlotsAvailable, cohortMatch.blacklist AS cohortBlacklist, cohortMatch.isEligible AS isEligibleCohort,
        NULL AS eligibilityRule, NULL AS result, NULL as recoverable, NULL AS passSpecificMessages, NULL AS passGeneralMessages,
        NULL AS warnSpecificMessages, NULL AS warnGeneralMessages, NULL AS undeterminedSpecificMessages,
        NULL AS undeterminedGeneralMessages, NULL AS failSpecificMessages, NULL AS failGeneralMessages
    FROM cohortMatch
    INNER JOIN trialMatch ON trialMatch.id = cohortMatch.trialMatchId
    INNER JOIN treatmentMatch ON treatmentMatch.id = trialMatch.treatmentMatchId
    LEFT JOIN trial ON trial.code = trialMatch.code
    WHERE cohortMatch.id NOT IN (SELECT DISTINCT cohortMatchId FROM evaluation WHERE NOT isnull(cohortMatchId))
    ORDER BY patientId, cohortId)
AS a
);