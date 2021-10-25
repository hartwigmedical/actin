package com.hartwig.actin.treatment.database;

import static org.junit.Assert.assertFalse;
import static org.junit.Assert.assertTrue;

import com.hartwig.actin.treatment.database.config.ImmutableCohortDefinitionConfig;
import com.hartwig.actin.treatment.database.config.ImmutableInclusionCriteriaConfig;
import com.hartwig.actin.treatment.database.config.ImmutableTrialDefinitionConfig;
import com.hartwig.actin.treatment.datamodel.EligibilityRule;

import org.apache.logging.log4j.util.Strings;
import org.jetbrains.annotations.NotNull;
import org.junit.Test;

public class TreatmentDatabaseValidatorTest {

    @Test
    public void testTreatmentDatabasesAreValid() {
        assertTrue(TreatmentDatabaseValidator.isValid(TestTreatmentDatabase.createMinimalTestTreatmentDatabase()));
        assertTrue(TreatmentDatabaseValidator.isValid(TestTreatmentDatabase.createProperTestTreatmentDatabase()));
    }

    @Test
    public void canDetectIllDefinedTreatmentDatabase() {
        assertFalse(TreatmentDatabaseValidator.isValid(createInvalidTreatmentDatabase()));
    }

    @NotNull
    private static TreatmentDatabase createInvalidTreatmentDatabase() {
        String trial1 = "trial 1";
        String trial2 = "trial 2";

        return ImmutableTreatmentDatabase.builder()
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
                        .eligibilityRule(EligibilityRule.IS_ADULT)
                        .build())
                .addInclusionCriteriaConfigs(ImmutableInclusionCriteriaConfig.builder()
                        .trialId(trial2)
                        .addAppliesToCohorts("A")
                        .eligibilityRule(EligibilityRule.IS_ADULT)
                        .build())
                .build();
    }
}