CREATE OR REPLACE VIEW trialEvaluation
AS (
SELECT * FROM (SELECT  referenceDate, referenceDateIsLive, sampleId, trialMatch.code AS trialId, trial.acronym AS trialAcronym, trial.open AS trialOpen,
        IF(trial.id IN (SELECT trialId FROM cohort),1,0) AS trialHasCohorts, trialMatch.isEligible AS isEligibleTrial,
        cohortMatch.code AS cohortId, cohortMatch.description AS cohortDescription, cohortMatch.open AS cohortOpen, cohortMatch.slotsAvailable AS cohortSlotsAvailable, cohortMatch.blacklist AS cohortBlacklist,
        cohortMatch.isEligible AS isEligibleCohort,
        eligibility AS eligibilityRule, result, recoverable, passSpecificMessages, passGeneralMessages, warnSpecificMessages, warnGeneralMessages,
        undeterminedSpecificMessages, undeterminedGeneralMessages, failSpecificMessages, failGeneralMessages
FROM evaluation
INNER JOIN trialMatch ON trialMatch.id = evaluation.trialMatchId
INNER JOIN treatmentMatch ON treatmentMatch.id = trialMatch.treatmentMatchId
INNER JOIN trial ON trial.code = trialMatch.code
LEFT JOIN cohortMatch ON trialMatch.id = cohortMatch.trialMatchId AND cohortMatch.Id = evaluation.cohortMatchId
    UNION
SELECT DISTINCT referenceDate, referenceDateIsLive, sampleId, trialMatch.code AS trialId, trial.acronym AS trialAcronym, trial.open AS trialOpen,
        IF(trial.id IN (SELECT trialId FROM cohort),1,0) AS trialHasCohorts, trialMatch.isEligible AS isEligibleTrial,
        cohortMatch.code AS cohortId, cohortMatch.description AS cohortDescription, cohortMatch.open AS cohortOpen, cohortMatch.slotsAvailable AS cohortSlotsAvailable, cohortMatch.blacklist AS cohortBlacklist,
        cohortMatch.isEligible AS isEligibleCohort,
        NULL AS eligibilityRule, NULL AS result, NULL as recoverable, NULL AS passSpecificMessages, NULL AS passGeneralMessages, NULL AS warnSpecificMessages, NULL AS warnGeneralMessages,
        NULL AS undeterminedSpecificMessages, NULL AS undeterminedGeneralMessages, NULL AS failSpecificMessages, NULL AS failGeneralMessages
FROM cohortMatch
INNER JOIN trialMatch ON trialMatch.id = cohortMatch.trialMatchId
INNER JOIN treatmentMatch ON treatmentMatch.id = trialMatch.treatmentMatchId
INNER JOIN trial ON trial.code = trialMatch.code
WHERE cohortMatch.id NOT IN (SELECT DISTINCT cohortMatchId FROM evaluation WHERE NOT isnull(cohortMatchId))
ORDER BY sampleId, cohortId) AS a
);

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