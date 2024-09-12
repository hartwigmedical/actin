package com.hartwig.actin.trial

import com.hartwig.actin.datamodel.trial.EligibilityRule
import com.hartwig.actin.trial.config.InclusionCriteriaConfig
import com.hartwig.actin.trial.config.InclusionCriteriaValidationError
import com.hartwig.actin.trial.config.TrialConfigDatabaseValidation
import com.hartwig.actin.trial.status.TrialStatusConfigValidationError
import com.hartwig.actin.trial.status.TrialStatusDatabaseValidation
import org.assertj.core.api.Assertions.assertThat
import org.junit.Test

class TrialIngestionStatusTest {

    private val inclusionCriterion = InclusionCriteriaConfig(
        trialId = "trial 1",
        referenceIds = setOf("I-01"),
        appliesToCohorts = setOf("B"),
        inclusionRule = EligibilityRule.IS_AT_LEAST_X_YEARS_OLD.toString()
    )

    @Test
    fun `Should return FAIL`() {
        val trialStatusDatabaseValidation = TrialStatusDatabaseValidation(
            trialStatusConfigValidationErrors = listOf(TrialStatusConfigValidationError("config", "msg")),
            trialStatusDatabaseValidationErrors = emptyList()
        )

        val trialValidationResult = TrialConfigDatabaseValidation(
            emptySet(),
            emptySet(),
            setOf(InclusionCriteriaValidationError(config = inclusionCriterion, message = "Not a valid inclusion criterion for trial")),
            emptySet(),
            emptySet()
        )
        assertThat(TrialIngestionStatus.from(trialStatusDatabaseValidation, trialValidationResult)).isEqualTo(TrialIngestionStatus.FAIL)
    }

    @Test
    fun `Should return PASS`() {
        val trialStatusDatabaseValidation = TrialStatusDatabaseValidation(
            trialStatusConfigValidationErrors = emptyList(),
            trialStatusDatabaseValidationErrors = emptyList()
        )
        val trialValidationResult = TrialConfigDatabaseValidation(
            emptySet(),
            emptySet(),
            emptySet(),
            emptySet(),
            emptySet()
        )
        assertThat(TrialIngestionStatus.from(trialStatusDatabaseValidation, trialValidationResult)).isEqualTo(TrialIngestionStatus.PASS)
    }

    @Test
    fun `Should return WARM for having status database validation errors`() {
        val trialStatusDatabaseValidation = TrialStatusDatabaseValidation(
            trialStatusConfigValidationErrors = listOf(TrialStatusConfigValidationError("config", "msg")),
            trialStatusDatabaseValidationErrors = emptyList()
        )
        val trialValidationResult = TrialConfigDatabaseValidation(
            emptySet(),
            emptySet(),
            emptySet(),
            emptySet(),
            emptySet()
        )
        assertThat(TrialIngestionStatus.from(trialStatusDatabaseValidation, trialValidationResult)).isEqualTo(TrialIngestionStatus.WARN)
    }
}
