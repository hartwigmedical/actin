CREATE OR REPLACE VIEW actin_pilot.trialEvaluation AS (
SELECT sampleId, trialMatch.code AS trialId, acronym AS trialAcronym, trialMatch.isEligible AS isEligibleTrial, cohortMatch.code AS cohortId, description AS cohortDescription, cohortMatch.isEligible AS isEligibleCohort, eligibility AS eligibilityRule, result, passSpecificMessages, passGeneralMessages, warnSpecificMessages, warnGeneralMessages, undeterminedSpecificMessages, undeterminedGeneralMessages, failSpecificMessages, failGeneralMessages
FROM evaluation
INNER JOIN trialMatch ON trialMatch.id=evaluation.trialMatchId
LEFT JOIN cohortMatch ON trialMatch.id=cohortMatch.trialMatchId AND cohortMatch.Id=evaluation.cohortMatchId
);

CREATE OR REPLACE VIEW actin_pilot.criteriaMapping AS (
SELECT trial.code AS trialId, acronym AS trialAcronym, cohort.code AS cohortId, description AS cohortDescription, reference.code AS criteriumCode, text AS criteriumText, display AS eligibilityRule
FROM eligibilityReferences
INNER JOIN
(SELECT DISTINCT id, trialId, cohortId, display FROM eligibility) AS a ON a.id = eligibilityReferences.eligibilityId
INNER JOIN reference ON eligibilityReferences.referenceId=reference.Id
INNER JOIN trial ON trial.id=a.trialId
LEFT JOIN cohort ON cohort.id=a.cohortId
ORDER BY trialId
);