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

CREATE OR REPLACE VIEW eligibleCohorts
AS (
SELECT DISTINCT patientId, trialId, trialAcronym, cohortDescription
    FROM trialEvaluation
    WHERE ((isEligibleTrial AND NOT trialHasCohorts AND trialOpen) OR (isEligibleTrial AND isEligibleCohort AND cohortOpen AND NOT cohortBlacklist))
);

CREATE OR REPLACE VIEW molecularDetails
AS (
SELECT * FROM (
SELECT  x.sampleId,
	    IF(driverType='HOTSPOT',"Mutation (Hotspot)",IF(driverType='BIALLELIC',"Mutation (Biallelic VUS)",IF(driverType='VUS',"Mutation (VUS)", driverType))) AS type,
	    x.event, concat(round(variantCopyNumber,1),"/",round(totalCopyNumber,1), " copies") AS details, driverLikelihood, group_concat(DISTINCT a.trialAcronym) AS actinTrials, group_concat(DISTINCT e.trial) AS externalTrials,
	    IF(group_concat(DISTINCT t.type) LIKE '%Approved%', "Approved", IF(group_concat(DISTINCT t.type) LIKE '%Experimental%', "Experimental", IF(group_concat(DISTINCT t.type) LIKE '%Pre-clinical%', "Pre-clinical", group_concat(DISTINCT t.type)))) AS treatmentEvidenceResponsive,
		IF(group_concat(DISTINCT y.type) LIKE '%Known%', "Known", IF(group_concat(DISTINCT y.type) LIKE '%Suspect%', "Suspected", group_concat(DISTINCT y.type))) AS treatmentEvidenceResistance
    FROM variant x
	LEFT JOIN (SELECT * FROM actinTrialEvidence WHERE isInclusionCriterion) AS a ON x.sampleId=a.sampleId AND x.event=a.event
	LEFT JOIN (SELECT * FROM externalTrialEvidence) AS e ON x.sampleId=e.sampleId AND x.event=e.event
	LEFT JOIN (SELECT * FROM treatmentEvidence WHERE isResponsive) AS t ON x.sampleId=t.sampleId AND x.event=t.event
	LEFT JOIN (SELECT * FROM treatmentEvidence WHERE NOT isResponsive) AS y ON x.sampleId=y.sampleId AND x.event=y.event
	GROUP BY 1,3
UNION
SELECT  x.sampleId,
	    IF(isPartial, "Amplification (partial)", "Amplification") AS type,
        x.event, concat(copies," copies") AS details, driverLikelihood, group_concat(DISTINCT a.trialAcronym) AS actinTrials, group_concat(DISTINCT e.trial) AS externalTrials,
	    IF(group_concat(DISTINCT t.type) LIKE '%Approved%', "Approved", IF(group_concat(DISTINCT t.type) LIKE '%Experimental%', "Experimental", IF(group_concat(DISTINCT t.type) LIKE '%Pre-clinical%', "Pre-clinical", group_concat(DISTINCT t.type)))) AS treatmentEvidenceResponsive,
		IF(group_concat(DISTINCT y.type) LIKE '%Known%', "Known", IF(group_concat(DISTINCT y.type) LIKE '%Suspect%', "Suspected", group_concat(DISTINCT y.type))) AS treatmentEvidenceResistance
	FROM amplification x
	LEFT JOIN (SELECT * FROM actinTrialEvidence WHERE isInclusionCriterion) AS a ON x.sampleId=a.sampleId AND x.event=a.event
	LEFT JOIN (SELECT * FROM externalTrialEvidence) AS e ON x.sampleId=e.sampleId AND x.event=e.event
	LEFT JOIN (SELECT * FROM treatmentEvidence WHERE isResponsive) AS t ON x.sampleId=t.sampleId AND x.event=t.event
	LEFT JOIN (SELECT * FROM treatmentEvidence WHERE NOT isResponsive) AS y ON x.sampleId=y.sampleId AND x.event=y.event
	GROUP BY 1,3
UNION
SELECT  x.sampleId,
	    IF(isPartial, "Loss (partial)", "Loss") AS type,
        x.event, NULL AS details, driverLikelihood, group_concat(DISTINCT a.trialAcronym) AS actinTrials, group_concat(DISTINCT e.trial) AS externalTrials,
	    IF(group_concat(DISTINCT t.type) LIKE '%Approved%', "Approved", IF(group_concat(DISTINCT t.type) LIKE '%Experimental%', "Experimental", IF(group_concat(DISTINCT t.type) LIKE '%Pre-clinical%', "Pre-clinical", group_concat(DISTINCT t.type)))) AS treatmentEvidenceResponsive,
		IF(group_concat(DISTINCT y.type) LIKE '%Known%', "Known", IF(group_concat(DISTINCT y.type) LIKE '%Suspect%', "Suspected", group_concat(DISTINCT y.type))) AS treatmentEvidenceResistance
    FROM loss x
	LEFT JOIN (SELECT * FROM actinTrialEvidence WHERE isInclusionCriterion) AS a ON x.sampleId=a.sampleId AND x.event=a.event
	LEFT JOIN (SELECT * FROM externalTrialEvidence) AS e ON x.sampleId=e.sampleId AND x.event=e.event
	LEFT JOIN (SELECT * FROM treatmentEvidence WHERE isResponsive) AS t ON x.sampleId=t.sampleId AND x.event=t.event
    LEFT JOIN (SELECT * FROM treatmentEvidence WHERE NOT isResponsive) AS y ON x.sampleId=y.sampleId AND x.event=y.event
	GROUP BY 1,3
UNION
SELECT  x.sampleId,
	    "Homozygous disruption" AS type,
	    x.event, NULL AS details, driverLikelihood, group_concat(DISTINCT a.trialAcronym) AS actinTrials, group_concat(DISTINCT e.trial) AS externalTrials,
	    IF(group_concat(DISTINCT t.type) LIKE '%Approved%', "Approved", IF(group_concat(DISTINCT t.type) LIKE '%Experimental%', "Experimental", IF(group_concat(DISTINCT t.type) LIKE '%Pre-clinical%', "Pre-clinical", group_concat(DISTINCT t.type)))) AS treatmentEvidenceResponsive,
		IF(group_concat(DISTINCT y.type) LIKE '%Known%', "Known", IF(group_concat(DISTINCT y.type) LIKE '%Suspect%', "Suspected", group_concat(DISTINCT y.type))) AS treatmentEvidenceResistance
	FROM homozygousDisruption x
	LEFT JOIN (SELECT * FROM actinTrialEvidence WHERE isInclusionCriterion) AS a ON x.sampleId=a.sampleId AND x.event=a.event
	LEFT JOIN (SELECT * FROM externalTrialEvidence) AS e ON x.sampleId=e.sampleId AND x.event=e.event
	LEFT JOIN (SELECT * FROM treatmentEvidence WHERE isResponsive) AS t ON x.sampleId=t.sampleId AND x.event=t.event
    LEFT JOIN (SELECT * FROM treatmentEvidence WHERE NOT isResponsive) AS y ON x.sampleId=y.sampleId AND x.event=y.event
	GROUP BY 1,3
UNION
SELECT  x.sampleId,
        IF(driverType='PROMISCUOUS',"Promiscuous fusion",IF(driverType='KNOWN',"Known fusion", driverType)) AS type,
        x.event, details, driverLikelihood, group_concat(DISTINCT a.trialAcronym) AS actinTrials, group_concat(DISTINCT e.trial) AS externalTrials,
	    IF(group_concat(DISTINCT t.type) LIKE '%Approved%', "Approved", IF(group_concat(DISTINCT t.type) LIKE '%Experimental%', "Experimental", IF(group_concat(DISTINCT t.type) LIKE '%Pre-clinical%', "Pre-clinical", group_concat(DISTINCT t.type)))) AS treatmentEvidenceResponsive,
		IF(group_concat(DISTINCT y.type) LIKE '%Known%', "Known", IF(group_concat(DISTINCT y.type) LIKE '%Suspect%', "Suspected", group_concat(DISTINCT y.type))) AS treatmentEvidenceResistance
	FROM fusion x
	LEFT JOIN (SELECT * FROM actinTrialEvidence WHERE isInclusionCriterion) AS a ON x.sampleId=a.sampleId AND x.event=a.event
	LEFT JOIN (SELECT * FROM externalTrialEvidence) AS e ON x.sampleId=e.sampleId AND x.event=e.event
	LEFT JOIN (SELECT * FROM treatmentEvidence WHERE isResponsive) AS t ON x.sampleId=t.sampleId AND x.event=t.event
    LEFT JOIN (SELECT * FROM treatmentEvidence WHERE NOT isResponsive) AS y ON x.sampleId=y.sampleId AND x.event=y.event
	GROUP BY 1,3
UNION
SELECT  x.sampleId,
        "Non-homozygous disruption" AS type,
        x.event, concat(round(junctionCopyNumber,1), " disr. / ", round(undisruptedCopyNumber,1), " undisr. copies"), driverLikelihood, group_concat(DISTINCT a.trialAcronym) AS actinTrials, group_concat(DISTINCT e.trial) AS externalTrials,
	    IF(group_concat(DISTINCT t.type) LIKE '%Approved%', "Approved", IF(group_concat(DISTINCT t.type) LIKE '%Experimental%', "Experimental", IF(group_concat(DISTINCT t.type) LIKE '%Pre-clinical%', "Pre-clinical", group_concat(DISTINCT t.type)))) AS treatmentEvidenceResponsive,
		IF(group_concat(DISTINCT y.type) LIKE '%Known%', "Known", IF(group_concat(DISTINCT y.type) LIKE '%Suspect%', "Suspected", group_concat(DISTINCT y.type))) AS treatmentEvidenceResistance
	FROM disruption x
	LEFT JOIN (SELECT * FROM actinTrialEvidence WHERE isInclusionCriterion) AS a ON x.sampleId=a.sampleId AND x.event=a.event
	LEFT JOIN (SELECT * FROM externalTrialEvidence) AS e ON x.sampleId=e.sampleId AND x.event=e.event
	LEFT JOIN (SELECT * FROM treatmentEvidence WHERE isResponsive) AS t ON x.sampleId=t.sampleId AND x.event=t.event
    LEFT JOIN (SELECT * FROM treatmentEvidence WHERE NOT isResponsive) AS y ON x.sampleId=y.sampleId AND x.event=y.event
	GROUP BY 1,3
UNION
SELECT  x.sampleId,
	    "Virus" AS type,
        x.event, concat(name, ", ", integrations, " integrations"), driverLikelihood, group_concat(DISTINCT a.trialAcronym) AS actinTrials, group_concat(DISTINCT e.trial) AS externalTrials,
	    IF(group_concat(DISTINCT t.type) LIKE '%Approved%', "Approved", IF(group_concat(DISTINCT t.type) LIKE '%Experimental%', "Experimental", IF(group_concat(DISTINCT t.type) LIKE '%Pre-clinical%', "Pre-clinical", group_concat(DISTINCT t.type)))) AS treatmentEvidenceResponsive,
		IF(group_concat(DISTINCT y.type) LIKE '%Known%', "Known", IF(group_concat(DISTINCT y.type) LIKE '%Suspect%', "Suspected", group_concat(DISTINCT y.type))) AS treatmentEvidenceResistance
	FROM virus x
	LEFT JOIN (SELECT * FROM actinTrialEvidence WHERE isInclusionCriterion) AS a ON x.sampleId=a.sampleId AND x.event=a.event
	LEFT JOIN (SELECT * FROM externalTrialEvidence) AS e ON x.sampleId=e.sampleId AND x.event=e.event
	LEFT JOIN (SELECT * FROM treatmentEvidence WHERE isResponsive) AS t ON x.sampleId=t.sampleId AND x.event=t.event
    LEFT JOIN (SELECT * FROM treatmentEvidence WHERE NOT isResponsive) AS y ON x.sampleId=y.sampleId AND x.event=y.event
	GROUP BY 1,3
ORDER BY 1,2,3)
AS a
);