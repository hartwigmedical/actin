package com.hartwig.actin.trial.config

import com.hartwig.actin.treatment.datamodel.EligibilityRule
import com.hartwig.actin.trial.CohortDefinitionValidationError
import com.hartwig.actin.trial.InclusionCriteriaValidationError
import com.hartwig.actin.trial.InclusionReferenceValidationError
import com.hartwig.actin.trial.TrialDefinitionValidationError
import com.hartwig.actin.trial.interpretation.TestEligibilityFactoryFactory
import org.assertj.core.api.Assertions.assertThat
import org.junit.Test

class TrialConfigDatabaseValidatorTest {

    @Test
    fun confirmTrialConfigDatabasesAreValid() {
        val validator: TrialConfigDatabaseValidator = createTestValidator()
        assertThat(validator.validate(TestTrialConfigDatabaseFactory.createMinimalTestTrialConfigDatabase()).hasErrors()).isFalse
        assertThat(validator.validate(TestTrialConfigDatabaseFactory.createProperTestTrialConfigDatabase()).hasErrors()).isFalse
    }

    @Test
    fun canDetectIllDefinedTrialConfigDatabase() {
        val validator: TrialConfigDatabaseValidator = createTestValidator()
        val validation = validator.validate(createInvalidTrialConfigDatabase())
        assertThat(validation.trialDefinitionValidationErrors).containsExactly(
            TrialDefinitionValidationError(config = TRIAL_DEFINITION_1, message = "Duplicated trial id of trial 1"),
            TrialDefinitionValidationError(
                config = TRIAL_DEFINITION_1,
                message = "Duplicated trial file id of trial_1"
            )
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

        assertThat(validation.inclusionReferenceValidationErrors).containsExactly(
            InclusionReferenceValidationError(
                config = INCLUSION_REFERENCE_CONFIG_1,
                message = "Reference 'I-01' defined on non-existing trial: 'does not exist'"
            ),
            InclusionReferenceValidationError(
                config = INCLUSION_REFERENCE_CONFIG_2,
                message = "Reference 'I-02' defined on non-existing trial: 'trial 2'"
            )
        )
    }

    companion object {

        private const val TRIAL_ID_1 = "trial 1"
        private const val TRIAL_ID_2 = "trial 2"
        val TRIAL_DEFINITION_1 = TestTrialDefinitionConfigFactory.MINIMAL.copy(trialId = TRIAL_ID_1, open = true)

        private val COHORT_DEFINITION_1 = TestCohortDefinitionConfigFactory.MINIMAL.copy(
            trialId = TRIAL_ID_1, evaluable = true, open = true, slotsAvailable = true, blacklist = false, cohortId = "A"
        )
        private val COHORT_DEFINITION_2 = TestCohortDefinitionConfigFactory.MINIMAL.copy(
            trialId = TRIAL_ID_2, evaluable = true, open = true, slotsAvailable = false, blacklist = false, cohortId = "A"
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
                    TRIAL_DEFINITION_1
                ),
                cohortDefinitionConfigs = listOf(
                    COHORT_DEFINITION_1, COHORT_DEFINITION_1, TestCohortDefinitionConfigFactory.MINIMAL.copy(
                        trialId = TRIAL_ID_2, evaluable = true, open = true, slotsAvailable = false, blacklist = false, cohortId = "A"
                    )
                ),
                inclusionCriteriaConfigs = listOf(INCLUSION_CRITERIA_1, INCLUSION_CRITERIA_2, INCLUSION_CRITERIA_3),
                inclusionCriteriaReferenceConfigs = listOf(INCLUSION_REFERENCE_CONFIG_1, INCLUSION_REFERENCE_CONFIG_2)
            )
        }

        private fun createTestValidator(): TrialConfigDatabaseValidator {
            return TrialConfigDatabaseValidator(TestEligibilityFactoryFactory.createTestEligibilityFactory())
        }
    }
}