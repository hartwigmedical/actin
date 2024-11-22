package com.hartwig.actin.trial.config

import com.hartwig.actin.datamodel.trial.EligibilityRule
import com.hartwig.actin.trial.interpretation.TestEligibilityFactoryFactory
import org.assertj.core.api.Assertions.assertThat
import org.junit.Test

class TrialConfigDatabaseValidatorTest {
    private val validator = TrialConfigDatabaseValidator(TestEligibilityFactoryFactory.createTestEligibilityFactory())

    @Test
    fun `Should confirm trial config databases are valid`() {
        assertThat(validator.validate(TestTrialConfigDatabaseFactory.createMinimalTestTrialConfigDatabase()).hasErrors()).isFalse
        assertThat(validator.validate(TestTrialConfigDatabaseFactory.createProperTestTrialConfigDatabase()).hasErrors()).isFalse
    }

    @Test
    fun `Should not validate inclusion criteria for not evaluable cohorts`() {
        val properConfig = TestTrialConfigDatabaseFactory.createProperTestTrialConfigDatabase()
        val invalidInclusionCriteriaConfig = properConfig.copy(
            inclusionCriteriaConfigs = properConfig.inclusionCriteriaConfigs.map { it.copy(inclusionRule = "INVALID_RULE[ABC]") },
        )
        assertThat(validator.validate(invalidInclusionCriteriaConfig).hasErrors()).isTrue

        val disabledCohortsConfig = invalidInclusionCriteriaConfig.copy(
            cohortDefinitionConfigs = invalidInclusionCriteriaConfig.cohortDefinitionConfigs.map { it.copy(evaluable = false) }
        )
        assertThat(validator.validate(disabledCohortsConfig).hasErrors()).isFalse
    }

    @Test
    fun `Should detect ill defined trial config database`() {
        val validation = validator.validate(createInvalidTrialConfigDatabase())
        assertThat(validation.trialDefinitionValidationErrors).containsExactly(
            TrialDefinitionValidationError(config = TRIAL_DEFINITION_1, message = "Duplicated trial id of trial 1"),
            TrialDefinitionValidationError(
                config = TRIAL_DEFINITION_1,
                message = "Duplicated trial file id of trial_1"
            ),
            TrialDefinitionValidationError(config = TRIAL_DEFINITION_3, message = "Invalid phase: 'invalid phase'")
        )
        assertThat(validation.cohortDefinitionValidationErrors).containsExactly(
            CohortDefinitionValidationError(
                config = COHORT_DEFINITION_1, message = "Cohort 'A' is duplicated."
            ),
            CohortDefinitionValidationError(
                config = COHORT_DEFINITION_2, message = "Cohort 'A' defined on non-existing trial: 'trial 2'"
            )
        )
        assertThat(validation.inclusionCriteriaValidationErrors).containsExactly(
            InclusionCriteriaValidationError(config = INCLUSION_CRITERIA_2, message = "Inclusion criterion defined on non-existing trial"),
            InclusionCriteriaValidationError(
                config = INCLUSION_CRITERIA_1,
                message = "Inclusion criterion defined on non-existing cohort 'B'"
            ),
            InclusionCriteriaValidationError(config = INCLUSION_CRITERIA_1, message = "Not a valid inclusion criterion for trial"),
            InclusionCriteriaValidationError(config = INCLUSION_CRITERIA_2, message = "Not a valid inclusion criterion for trial"),
            InclusionCriteriaValidationError(config = INCLUSION_CRITERIA_3, message = "Not a valid inclusion criterion for trial")
        )

        assertThat(validation.inclusionCriteriaReferenceValidationErrors).containsExactly(
            InclusionCriteriaReferenceValidationError(
                config = INCLUSION_REFERENCE_CONFIG_1,
                message = "Reference 'I-01' defined on non-existing trial: 'does not exist'"
            ),
            InclusionCriteriaReferenceValidationError(
                config = INCLUSION_REFERENCE_CONFIG_2,
                message = "Reference 'I-02' defined on non-existing trial: 'trial 2'"
            )
        )
    }

    @Test
    fun `Should detect invalid unused rules to keep`() {
        val validation = validator.validate(createInvalidTrialConfigDatabase())
        assertThat(validation.unusedRulesToKeepValidationErrors).containsExactly(UnusedRulesToKeepValidationError(config = "invalid rule"))
    }

    companion object {
        private const val TRIAL_ID_1 = "trial 1"
        private const val TRIAL_ID_2 = "trial 2"
        private const val TRIAL_ID_3 = "trial 3"

        val TRIAL_DEFINITION_1 = TestTrialDefinitionConfigFactory.MINIMAL.copy(trialId = TRIAL_ID_1, open = true)
        val TRIAL_DEFINITION_3 = TestTrialDefinitionConfigFactory.MINIMAL.copy(trialId = TRIAL_ID_3, open = true, phase = "invalid phase")

        private val COHORT_DEFINITION_1 = TestCohortDefinitionConfigFactory.MINIMAL.copy(
            trialId = TRIAL_ID_1, evaluable = true, open = true, slotsAvailable = true, ignore = false, cohortId = "A"
        )
        private val COHORT_DEFINITION_2 = TestCohortDefinitionConfigFactory.MINIMAL.copy(
            trialId = TRIAL_ID_2, evaluable = true, open = true, slotsAvailable = false, ignore = false, cohortId = "A"
        )

        private val INCLUSION_CRITERIA_1 = InclusionCriteriaConfig(
            trialId = TRIAL_ID_1,
            referenceIds = setOf("I-01"),
            appliesToCohorts = setOf("B"),
            inclusionRule = EligibilityRule.IS_AT_LEAST_X_YEARS_OLD.toString()
        )

        private val INCLUSION_CRITERIA_2 = InclusionCriteriaConfig(
            trialId = TRIAL_ID_2,
            referenceIds = setOf("I-02"),
            appliesToCohorts = setOf("A"),
            inclusionRule = EligibilityRule.IS_AT_LEAST_X_YEARS_OLD.toString()
        )

        private val INCLUSION_CRITERIA_3 = InclusionCriteriaConfig(
            trialId = TRIAL_ID_1,
            referenceIds = emptySet(),
            appliesToCohorts = setOf("A"),
            inclusionRule = "not a valid inclusion criterion"
        )

        private val INCLUSION_REFERENCE_CONFIG_1 = InclusionCriteriaReferenceConfig(
            trialId = "does not exist", referenceId = "I-01", referenceText = "irrelevant"
        )

        private val INCLUSION_REFERENCE_CONFIG_2 = InclusionCriteriaReferenceConfig(
            trialId = TRIAL_ID_2,
            referenceId = "I-02",
            referenceText = "Some rule",
        )

        private fun createInvalidTrialConfigDatabase(): TrialConfigDatabase {

            return TrialConfigDatabase(
                trialDefinitionConfigs = listOf(
                    TRIAL_DEFINITION_1,
                    TRIAL_DEFINITION_1,
                    TRIAL_DEFINITION_3
                ),
                cohortDefinitionConfigs = listOf(
                    COHORT_DEFINITION_1, COHORT_DEFINITION_1, TestCohortDefinitionConfigFactory.MINIMAL.copy(
                        trialId = TRIAL_ID_2, evaluable = true, open = true, slotsAvailable = false, ignore = false, cohortId = "A"
                    )
                ),
                inclusionCriteriaConfigs = listOf(INCLUSION_CRITERIA_1, INCLUSION_CRITERIA_2, INCLUSION_CRITERIA_3),
                inclusionCriteriaReferenceConfigs = listOf(INCLUSION_REFERENCE_CONFIG_1, INCLUSION_REFERENCE_CONFIG_2),
                unusedRulesToKeep = listOf("invalid rule")
            )
        }
    }
}