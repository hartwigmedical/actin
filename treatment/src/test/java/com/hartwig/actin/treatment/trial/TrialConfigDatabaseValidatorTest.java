package com.hartwig.actin.treatment.trial;

import static org.junit.Assert.assertFalse;
import static org.junit.Assert.assertTrue;

import com.hartwig.actin.treatment.datamodel.EligibilityRule;
import com.hartwig.actin.treatment.trial.config.ImmutableInclusionCriteriaConfig;
import com.hartwig.actin.treatment.trial.config.ImmutableInclusionCriteriaReferenceConfig;
import com.hartwig.actin.treatment.trial.config.TestCohortDefinitionConfigFactory;
import com.hartwig.actin.treatment.trial.config.TestTrialConfigDatabaseFactory;
import com.hartwig.actin.treatment.trial.config.TestTrialDefinitionConfigFactory;

import org.jetbrains.annotations.NotNull;
import org.junit.Test;

public class TrialConfigDatabaseValidatorTest {

    @Test
    public void confirmTrialConfigDatabasesAreValid() {
        TrialConfigDatabaseValidator validator = createTestValidator();
        assertTrue(validator.isValid(TestTrialConfigDatabaseFactory.createMinimalTestTrialConfigDatabase()));
        assertTrue(validator.isValid(TestTrialConfigDatabaseFactory.createProperTestTrialConfigDatabase()));
    }

    @Test
    public void canDetectIllDefinedTrialConfigDatabase() {
        TrialConfigDatabaseValidator validator = createTestValidator();
        assertFalse(validator.isValid(createInvalidTrialConfigDatabase()));
    }

    @NotNull
    private static TrialConfigDatabase createInvalidTrialConfigDatabase() {
        String trial1 = "trial 1";
        String trial2 = "trial 2";

        return ImmutableTrialConfigDatabase.builder()
                .addTrialDefinitionConfigs(TestTrialDefinitionConfigFactory.builder().trialId(trial1).open(true).build())
                .addTrialDefinitionConfigs(TestTrialDefinitionConfigFactory.builder().trialId(trial1).open(true).build())

                .addCohortDefinitionConfigs(TestCohortDefinitionConfigFactory.builder()
                        .trialId(trial1)
                        .evaluable(true)
                        .open(true)
                        .slotsAvailable(true)
                        .blacklist(false)
                        .cohortId("A")
                        .build())
                .addCohortDefinitionConfigs(TestCohortDefinitionConfigFactory.builder()
                        .trialId(trial1)
                        .evaluable(true)
                        .open(true)
                        .slotsAvailable(true)
                        .blacklist(false)
                        .cohortId("A")
                        .build())
                .addCohortDefinitionConfigs(TestCohortDefinitionConfigFactory.builder()
                        .trialId(trial2)
                        .evaluable(true)
                        .open(true)
                        .slotsAvailable(false)
                        .blacklist(false)
                        .cohortId("A")
                        .build())

                .addInclusionCriteriaConfigs(ImmutableInclusionCriteriaConfig.builder()
                        .trialId(trial1)
                        .addReferenceIds("I-01")
                        .addAppliesToCohorts("B")
                        .inclusionRule(EligibilityRule.IS_AT_LEAST_X_YEARS_OLD.toString())
                        .build())
                .addInclusionCriteriaConfigs(ImmutableInclusionCriteriaConfig.builder()
                        .trialId(trial2)
                        .addReferenceIds("I-02")
                        .addAppliesToCohorts("A")
                        .inclusionRule(EligibilityRule.IS_AT_LEAST_X_YEARS_OLD.toString())
                        .build())
                .addInclusionCriteriaConfigs(ImmutableInclusionCriteriaConfig.builder()
                        .trialId(trial1)
                        .addAppliesToCohorts("A")
                        .inclusionRule("not a valid inclusion criterion")
                        .build())

                .addInclusionCriteriaReferenceConfigs(ImmutableInclusionCriteriaReferenceConfig.builder()
                        .trialId("does not exist")
                        .referenceId("I-01")
                        .referenceText("irrelevant")
                        .build())
                .addInclusionCriteriaReferenceConfigs(ImmutableInclusionCriteriaReferenceConfig.builder()
                        .trialId(trial2)
                        .referenceId("I-02")
                        .referenceText("Some rule")
                        .build())

                .build();
    }

    @NotNull
    private static TrialConfigDatabaseValidator createTestValidator() {
        return new TrialConfigDatabaseValidator(TestEligibilityFactoryFactory.createTestEligibilityFactory());
    }
}