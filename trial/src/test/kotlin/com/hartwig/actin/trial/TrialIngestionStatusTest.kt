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
    fun `Should return FAIL due to the existence of trial status config validation errors`() {
        val trialStatusDatabaseValidation = TrialStatusDatabaseValidation(
            trialStatusConfigValidationErrors = listOf(TrialStatusConfigValidationError("config", "msg")),
            trialStatusDatabaseValidationErrors = emptyList()
        )

        val trialConfigDatabaseValidation = TrialConfigDatabaseValidation(
            emptySet(),
            emptySet(),
            setOf(InclusionCriteriaValidationError(config = inclusionCriterion, message = "Not a valid inclusion criterion for trial")),
            emptySet(),
            emptySet()
        )
        assertThat(TrialIngestionStatus.from(trialConfigDatabaseValidation, trialStatusDatabaseValidation, emptySet())).isEqualTo(
            TrialIngestionStatus.FAIL
        )
    }

    @Test
    fun `Should return PASS as there are no trial config or trial status errors`() {
        val trialStatusDatabaseValidation = TrialStatusDatabaseValidation(
            trialStatusConfigValidationErrors = emptyList(),
            trialStatusDatabaseValidationErrors = emptyList()
        )
        val trialConfigDatabaseValidation = TrialConfigDatabaseValidation(
            emptySet(),
            emptySet(),
            emptySet(),
            emptySet(),
            emptySet()
        )
        assertThat(TrialIngestionStatus.from(trialConfigDatabaseValidation, trialStatusDatabaseValidation, emptySet())).isEqualTo(
            TrialIngestionStatus.PASS
        )
    }

    @Test
    fun `Should return WARN for having status database validation errors`() {
        val trialStatusDatabaseValidation = TrialStatusDatabaseValidation(
            trialStatusConfigValidationErrors = listOf(TrialStatusConfigValidationError("config", "msg")),
            trialStatusDatabaseValidationErrors = emptyList()
        )
        val trialConfigDatabaseValidation = TrialConfigDatabaseValidation(
            emptySet(),
            emptySet(),
            emptySet(),
            emptySet(),
            emptySet()
        )
        assertThat(TrialIngestionStatus.from(trialConfigDatabaseValidation, trialStatusDatabaseValidation, emptySet())).isEqualTo(
            TrialIngestionStatus.WARN
        )
    }

    @Test
    fun `Should return WARN due to the existance of unused rules`() {
        val trialStatusDatabaseValidation = TrialStatusDatabaseValidation(
            trialStatusConfigValidationErrors = emptyList(),
            trialStatusDatabaseValidationErrors = emptyList()
        )
        val trialConfigDatabaseValidation = TrialConfigDatabaseValidation(
            emptySet(),
            emptySet(),
            emptySet(),
            emptySet(),
            emptySet()
        )
        val unusedRules = setOf("Rule1", "Rule2")
        assertThat(TrialIngestionStatus.from(trialConfigDatabaseValidation, trialStatusDatabaseValidation, unusedRules)).isEqualTo(
            TrialIngestionStatus.WARN
        )
    }
}
