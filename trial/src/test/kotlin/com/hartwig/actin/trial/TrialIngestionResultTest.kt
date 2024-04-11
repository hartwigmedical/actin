package com.hartwig.actin.trial

import com.hartwig.actin.trial.config.CohortDefinitionValidationError
import com.hartwig.actin.trial.config.InclusionCriteriaConfig
import com.hartwig.actin.trial.config.InclusionCriteriaReferenceConfig
import com.hartwig.actin.trial.config.InclusionCriteriaValidationError
import com.hartwig.actin.trial.config.InclusionReferenceValidationError
import com.hartwig.actin.trial.config.TestCohortDefinitionConfigFactory
import com.hartwig.actin.trial.config.TestTrialDefinitionConfigFactory
import com.hartwig.actin.trial.config.TrialDatabaseValidation
import com.hartwig.actin.trial.config.TrialDefinitionValidationError
import com.hartwig.actin.trial.config.UnusedRuleToKeepError
import com.hartwig.actin.trial.datamodel.EligibilityRule
import com.hartwig.actin.trial.status.TrialStatusDatabaseValidation
import com.hartwig.actin.trial.status.TrialStatusDatabaseValidationError
import com.hartwig.actin.trial.status.config.TestTrialStatusDatabaseEntryFactory
import com.hartwig.actin.util.json.GsonSerializer
import org.junit.Test

private const val TRIAL_ID_1 = "trial 1"

class TrialIngestionResultTest {
    private val inclusionCriterion = InclusionCriteriaConfig(
        trialId = TRIAL_ID_1,
        referenceIds = setOf("I-01"),
        appliesToCohorts = setOf("B"),
        inclusionRule = EligibilityRule.IS_AT_LEAST_X_YEARS_OLD.toString()
    )
    private val inclusionReference = InclusionCriteriaReferenceConfig(
        trialId = "does not exist", referenceId = "I-01", referenceText = "irrelevant"
    )
    private val trialDefinition = TestTrialDefinitionConfigFactory.MINIMAL.copy(trialId = TRIAL_ID_1, open = true)

    private val cohortDefinition = TestCohortDefinitionConfigFactory.MINIMAL.copy(
        trialId = TRIAL_ID_1, evaluable = true, open = true, slotsAvailable = true, blacklist = false, cohortId = "A"
    )

    @Test
    fun `Should serialize trial ingestion result`() {
        val result = TrialIngestionResult(
            ingestionStatus = TrialIngestionStatus.FAIL,
            trialStatusDatabaseValidation = TrialStatusDatabaseValidation(
                trialDefinitionValidationErrors = listOf(TrialDefinitionValidationError(TestTrialDefinitionConfigFactory.MINIMAL, "msg")),
                trialStatusDatabaseValidationErrors = listOf(
                    TrialStatusDatabaseValidationError(
                        TestTrialStatusDatabaseEntryFactory.MINIMAL,
                        "msg"
                    )
                ),
            ),
            trialValidationResult = TrialDatabaseValidation(
                setOf(InclusionCriteriaValidationError(config = inclusionCriterion, message = "Not a valid inclusion criterion for trial")),
                setOf(
                    InclusionReferenceValidationError(
                        config = inclusionReference, message = "Reference 'I-01' defined on non-existing trial: 'does not exist'"
                    )
                ),
                setOf(CohortDefinitionValidationError(config = cohortDefinition, message = "Cohort 'A' is duplicated.")),
                setOf(TrialDefinitionValidationError(config = trialDefinition, message = "Duplicated trial id of trial 1")),
                setOf(UnusedRuleToKeepError(config = "invalid rule"))
            ),
            trials = emptyList()
        )
        GsonSerializer.create().toJson(result)
    }
}