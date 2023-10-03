package com.hartwig.actin.trial.config

import com.hartwig.actin.treatment.datamodel.EligibilityRule
import com.hartwig.actin.trial.interpretation.TestEligibilityFactoryFactory
import org.assertj.core.api.Assertions.assertThat
import org.junit.Test

class TrialConfigDatabaseValidatorTest {

    @Test
    fun confirmTrialConfigDatabasesAreValid() {
        val validator: TrialConfigDatabaseValidator = createTestValidator()
        assertThat(validator.isValid(TestTrialConfigDatabaseFactory.createMinimalTestTrialConfigDatabase())).isTrue
        assertThat(validator.isValid(TestTrialConfigDatabaseFactory.createProperTestTrialConfigDatabase())).isTrue
    }

    @Test
    fun canDetectIllDefinedTrialConfigDatabase() {
        val validator: TrialConfigDatabaseValidator = createTestValidator()
        assertThat(validator.isValid(createInvalidTrialConfigDatabase())).isFalse
    }

    companion object {
        private fun createInvalidTrialConfigDatabase(): TrialConfigDatabase {
            val trial1 = "trial 1"
            val trial2 = "trial 2"
            return TrialConfigDatabase(
                trialDefinitionConfigs = listOf(
                    TestTrialDefinitionConfigFactory.MINIMAL.copy(trialId = trial1, open = true),
                    TestTrialDefinitionConfigFactory.MINIMAL.copy(trialId = trial1, open = true)
                ), cohortDefinitionConfigs = listOf(
                    TestCohortDefinitionConfigFactory.MINIMAL.copy(
                        trialId = trial1, evaluable = true, open = true, slotsAvailable = true, blacklist = false, cohortId = "A"
                    ), TestCohortDefinitionConfigFactory.MINIMAL.copy(
                        trialId = trial1, evaluable = true, open = true, slotsAvailable = true, blacklist = false, cohortId = "A"
                    ), TestCohortDefinitionConfigFactory.MINIMAL.copy(
                        trialId = trial2, evaluable = true, open = true, slotsAvailable = false, blacklist = false, cohortId = "A"
                    )
                ), inclusionCriteriaConfigs = listOf(
                    InclusionCriteriaConfig(
                        trialId = trial1,
                        referenceIds = setOf("I-01"),
                        appliesToCohorts = setOf("B"),
                        inclusionRule = EligibilityRule.IS_AT_LEAST_X_YEARS_OLD.toString()
                    ), InclusionCriteriaConfig(
                        trialId = trial2,
                        referenceIds = setOf("I-02"),
                        appliesToCohorts = setOf("A"),
                        inclusionRule = EligibilityRule.IS_AT_LEAST_X_YEARS_OLD.toString()
                    ), InclusionCriteriaConfig(
                        trialId = trial1,
                        referenceIds = emptySet(),
                        appliesToCohorts = setOf("A"),
                        inclusionRule = "not a valid inclusion criterion"
                    )
                ), inclusionCriteriaReferenceConfigs = listOf(
                    InclusionCriteriaReferenceConfig(
                        trialId = "does not exist", referenceId = "I-01", referenceText = "irrelevant"
                    ), InclusionCriteriaReferenceConfig(
                        trialId = trial2,
                        referenceId = "I-02",
                        referenceText = "Some rule",
                    )
                )
            )
        }

        private fun createTestValidator(): TrialConfigDatabaseValidator {
            return TrialConfigDatabaseValidator(TestEligibilityFactoryFactory.createTestEligibilityFactory())
        }
    }
}