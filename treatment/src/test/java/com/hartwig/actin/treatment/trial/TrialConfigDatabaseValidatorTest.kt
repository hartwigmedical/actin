package com.hartwig.actin.treatment.trial

import com.hartwig.actin.treatment.datamodel.EligibilityRule
import com.hartwig.actin.treatment.trial.config.ImmutableCohortDefinitionConfig
import com.hartwig.actin.treatment.trial.config.ImmutableInclusionCriteriaConfig
import com.hartwig.actin.treatment.trial.config.ImmutableInclusionCriteriaReferenceConfig
import com.hartwig.actin.treatment.trial.config.ImmutableTrialDefinitionConfig
import com.hartwig.actin.treatment.trial.config.TestTrialConfigFactory
import org.apache.logging.log4j.util.Strings
import org.junit.Assert
import org.junit.Test

class TrialConfigDatabaseValidatorTest {
    @Test
    fun confirmTrialConfigDatabasesAreValid() {
        val validator = createTestValidator()
        Assert.assertTrue(validator.isValid(TestTrialConfigFactory.createMinimalTestTrialConfigDatabase()))
        Assert.assertTrue(validator.isValid(TestTrialConfigFactory.createProperTestTrialConfigDatabase()))
    }

    @Test
    fun canDetectIllDefinedTrialConfigDatabase() {
        val validator = createTestValidator()
        Assert.assertFalse(validator.isValid(createInvalidTrialConfigDatabase()))
    }

    companion object {
        private fun createInvalidTrialConfigDatabase(): TrialConfigDatabase {
            val trial1 = "trial 1"
            val trial2 = "trial 2"
            return ImmutableTrialConfigDatabase.builder()
                .addTrialDefinitionConfigs(
                    ImmutableTrialDefinitionConfig.builder()
                        .trialId(trial1)
                        .open(true)
                        .acronym(Strings.EMPTY)
                        .title(Strings.EMPTY)
                        .build()
                )
                .addTrialDefinitionConfigs(
                    ImmutableTrialDefinitionConfig.builder()
                        .trialId(trial1)
                        .open(true)
                        .acronym(Strings.EMPTY)
                        .title(Strings.EMPTY)
                        .build()
                )
                .addCohortDefinitionConfigs(
                    ImmutableCohortDefinitionConfig.builder()
                        .trialId(trial1)
                        .evaluable(true)
                        .open(true)
                        .slotsAvailable(true)
                        .blacklist(false)
                        .cohortId("A")
                        .description(Strings.EMPTY)
                        .build()
                )
                .addCohortDefinitionConfigs(
                    ImmutableCohortDefinitionConfig.builder()
                        .trialId(trial1)
                        .evaluable(true)
                        .open(true)
                        .slotsAvailable(true)
                        .blacklist(false)
                        .cohortId("A")
                        .description(Strings.EMPTY)
                        .build()
                )
                .addCohortDefinitionConfigs(
                    ImmutableCohortDefinitionConfig.builder()
                        .trialId(trial2)
                        .evaluable(true)
                        .open(true)
                        .slotsAvailable(false)
                        .blacklist(false)
                        .cohortId("A")
                        .description(Strings.EMPTY)
                        .build()
                )
                .addInclusionCriteriaConfigs(
                    ImmutableInclusionCriteriaConfig.builder()
                        .trialId(trial1)
                        .addReferenceIds("I-01")
                        .addAppliesToCohorts("B")
                        .inclusionRule(EligibilityRule.IS_AT_LEAST_X_YEARS_OLD.toString())
                        .build()
                )
                .addInclusionCriteriaConfigs(
                    ImmutableInclusionCriteriaConfig.builder()
                        .trialId(trial2)
                        .addReferenceIds("I-02")
                        .addAppliesToCohorts("A")
                        .inclusionRule(EligibilityRule.IS_AT_LEAST_X_YEARS_OLD.toString())
                        .build()
                )
                .addInclusionCriteriaConfigs(
                    ImmutableInclusionCriteriaConfig.builder()
                        .trialId(trial1)
                        .addAppliesToCohorts("A")
                        .inclusionRule("not a valid inclusion criterion")
                        .build()
                )
                .addInclusionCriteriaReferenceConfigs(
                    ImmutableInclusionCriteriaReferenceConfig.builder()
                        .trialId("does not exist")
                        .referenceId("I-01")
                        .referenceText("irrelevant")
                        .build()
                )
                .addInclusionCriteriaReferenceConfigs(
                    ImmutableInclusionCriteriaReferenceConfig.builder()
                        .trialId(trial2)
                        .referenceId("I-02")
                        .referenceText("Some rule")
                        .build()
                )
                .build()
        }

        private fun createTestValidator(): TrialConfigDatabaseValidator {
            return TrialConfigDatabaseValidator(TestEligibilityFactoryFactory.createTestEligibilityFactory())
        }
    }
}