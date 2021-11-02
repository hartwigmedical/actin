package com.hartwig.actin.treatment.trial;

import static org.junit.Assert.assertFalse;
import static org.junit.Assert.assertTrue;

import com.hartwig.actin.treatment.datamodel.EligibilityRule;
import com.hartwig.actin.treatment.trial.config.ImmutableCohortDefinitionConfig;
import com.hartwig.actin.treatment.trial.config.ImmutableInclusionCriteriaConfig;
import com.hartwig.actin.treatment.trial.config.ImmutableTrialDefinitionConfig;
import com.hartwig.actin.treatment.trial.config.TestTrialConfigFactory;

import org.apache.logging.log4j.util.Strings;
import org.jetbrains.annotations.NotNull;
import org.junit.Test;

public class TrialConfigDatabaseValidatorTest {

    @Test
    public void confirmTrialConfigDatabasesAreValid() {
        assertTrue(TrialConfigDatabaseValidator.isValid(TestTrialConfigFactory.createMinimalTestTrialConfigDatabase()));
        assertTrue(TrialConfigDatabaseValidator.isValid(TestTrialConfigFactory.createProperTestTrialConfigDatabase()));
    }

    @Test
    public void canDetectIllDefinedTrialConfigDatabase() {
        assertFalse(TrialConfigDatabaseValidator.isValid(createInvalidTrialConfigDatabase()));
    }

    @NotNull
    private static TrialConfigDatabase createInvalidTrialConfigDatabase() {
        String trial1 = "trial 1";
        String trial2 = "trial 2";

        return ImmutableTrialConfigDatabase.builder()
                .addTrialDefinitionConfigs(ImmutableTrialDefinitionConfig.builder()
                        .trialId(trial1)
                        .acronym(Strings.EMPTY)
                        .title(Strings.EMPTY)
                        .build())
                .addTrialDefinitionConfigs(ImmutableTrialDefinitionConfig.builder()
                        .trialId(trial1)
                        .acronym(Strings.EMPTY)
                        .title(Strings.EMPTY)
                        .build())
                .addCohortDefinitionConfigs(ImmutableCohortDefinitionConfig.builder()
                        .trialId(trial1)
                        .open(true)
                        .cohortId("A")
                        .description(Strings.EMPTY)
                        .build())
                .addCohortDefinitionConfigs(ImmutableCohortDefinitionConfig.builder()
                        .trialId(trial1)
                        .open(true)
                        .cohortId("A")
                        .description(Strings.EMPTY)
                        .build())
                .addCohortDefinitionConfigs(ImmutableCohortDefinitionConfig.builder()
                        .trialId(trial2)
                        .open(true)
                        .cohortId("A")
                        .description(Strings.EMPTY)
                        .build())
                .addInclusionCriteriaConfigs(ImmutableInclusionCriteriaConfig.builder()
                        .trialId(trial1)
                        .addAppliesToCohorts("B")
                        .inclusionCriterion(EligibilityRule.IS_AT_LEAST_18_YEARS_OLD.toString())
                        .build())
                .addInclusionCriteriaConfigs(ImmutableInclusionCriteriaConfig.builder()
                        .trialId(trial2)
                        .addAppliesToCohorts("A")
                        .inclusionCriterion(EligibilityRule.IS_AT_LEAST_18_YEARS_OLD.toString())
                        .build())
                .addInclusionCriteriaConfigs(ImmutableInclusionCriteriaConfig.builder()
                        .trialId(trial1)
                        .addAppliesToCohorts("A")
                        .inclusionCriterion("not a valid inclusion criterion")
                        .build())
                .build();
    }
}