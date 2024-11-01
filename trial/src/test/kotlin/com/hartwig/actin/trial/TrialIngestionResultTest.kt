package com.hartwig.actin.trial

import com.hartwig.actin.datamodel.trial.EligibilityRule
import com.hartwig.actin.trial.config.CohortDefinitionValidationError
import com.hartwig.actin.trial.config.InclusionCriteriaConfig
import com.hartwig.actin.trial.config.InclusionCriteriaReferenceConfig
import com.hartwig.actin.trial.config.InclusionCriteriaReferenceValidationError
import com.hartwig.actin.trial.config.InclusionCriteriaValidationError
import com.hartwig.actin.trial.config.TestCohortDefinitionConfigFactory
import com.hartwig.actin.trial.config.TestTrialDefinitionConfigFactory
import com.hartwig.actin.trial.config.TrialConfigDatabaseValidation
import com.hartwig.actin.trial.config.TrialDefinitionValidationError
import com.hartwig.actin.trial.config.UnusedRulesToKeepValidationError
import com.hartwig.actin.trial.status.TrialStatusConfigValidationError
import com.hartwig.actin.trial.status.TrialStatusDatabaseValidation
import com.hartwig.actin.trial.status.TrialStatusDatabaseValidationError
import com.hartwig.actin.trial.status.config.TestTrialStatusDatabaseEntryFactory
import com.hartwig.actin.util.json.GsonSerializer
import org.assertj.core.api.Assertions.assertThat
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
        trialId = TRIAL_ID_1, evaluable = true, open = true, slotsAvailable = true, ignore = false, cohortId = "A"
    )

    @Test
    fun `Should serialize trial ingestion result`() {
        val result = TrialIngestionResult(
            ingestionStatus = TrialIngestionStatus.FAIL,
            trialStatusDatabaseValidation = TrialStatusDatabaseValidation(
                trialStatusConfigValidationErrors = listOf(TrialStatusConfigValidationError("config", "msg")),
                trialStatusDatabaseValidationErrors = listOf(
                    TrialStatusDatabaseValidationError(
                        TestTrialStatusDatabaseEntryFactory.MINIMAL,
                        "msg"
                    )
                )
            ),
            trialConfigDatabaseValidation = TrialConfigDatabaseValidation(
                setOf(TrialDefinitionValidationError(config = trialDefinition, message = "Duplicated trial id of trial 1")),
                setOf(CohortDefinitionValidationError(config = cohortDefinition, message = "Cohort 'A' is duplicated.")),
                setOf(InclusionCriteriaValidationError(config = inclusionCriterion, message = "Not a valid inclusion criterion for trial")),
                setOf(
                    InclusionCriteriaReferenceValidationError(
                        config = inclusionReference, message = "Reference 'I-01' defined on non-existing trial: 'does not exist'"
                    )
                ),
                setOf(UnusedRulesToKeepValidationError(config = "invalid rule"))
            ),
            trials = emptyList(),
            unusedRules = setOf("unused rule"),
        )

        val json = GsonSerializer.create().toJson(result)

        assertThat(json.startsWith("{\"ingestionStatus\":\"FAIL\"")).isTrue()
        assertThat(json.endsWith("\"unusedRules\":[\"unused rule\"]}")).isTrue()
    }
}