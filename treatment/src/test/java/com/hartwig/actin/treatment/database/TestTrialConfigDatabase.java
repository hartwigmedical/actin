package com.hartwig.actin.treatment.database;

import java.util.List;

import com.google.common.collect.Lists;
import com.hartwig.actin.treatment.database.config.CohortDefinitionConfig;
import com.hartwig.actin.treatment.database.config.ImmutableCohortDefinitionConfig;
import com.hartwig.actin.treatment.database.config.ImmutableInclusionCriteriaConfig;
import com.hartwig.actin.treatment.database.config.ImmutableTrialDefinitionConfig;
import com.hartwig.actin.treatment.database.config.InclusionCriteriaConfig;
import com.hartwig.actin.treatment.database.config.TrialDefinitionConfig;
import com.hartwig.actin.treatment.datamodel.EligibilityRule;

import org.jetbrains.annotations.NotNull;

public final class TestTrialConfigDatabase {

    private static final String TEST_TRIAL_ID = "TestTrial";

    private TestTrialConfigDatabase() {
    }

    @NotNull
    public static TrialConfigDatabase createMinimalTestTrialConfigDatabase() {
        return ImmutableTrialConfigDatabase.builder().build();
    }

    @NotNull
    public static TrialConfigDatabase createProperTestTrialConfigDatabase() {
        return ImmutableTrialConfigDatabase.builder()
                .from(createMinimalTestTrialConfigDatabase())
                .trialDefinitionConfigs(createTestTrialDefinitionConfigs())
                .cohortDefinitionConfigs(createTestCohortDefinitionConfigs())
                .inclusionCriteriaConfigs(createTestInclusionCriteriaConfigs())
                .build();
    }

    @NotNull
    private static List<TrialDefinitionConfig> createTestTrialDefinitionConfigs() {
        List<TrialDefinitionConfig> configs = Lists.newArrayList();

        configs.add(ImmutableTrialDefinitionConfig.builder()
                .trialId(TEST_TRIAL_ID)
                .acronym("Acronym-" + TEST_TRIAL_ID)
                .title("Title for " + TEST_TRIAL_ID)
                .build());

        return configs;
    }

    @NotNull
    private static List<CohortDefinitionConfig> createTestCohortDefinitionConfigs() {
        List<CohortDefinitionConfig> configs = Lists.newArrayList();

        ImmutableCohortDefinitionConfig.Builder builder = ImmutableCohortDefinitionConfig.builder().trialId(TEST_TRIAL_ID).open(true);

        configs.add(builder.cohortId("A").description("Cohort A").build());
        configs.add(builder.cohortId("B").description("Cohort B").build());
        configs.add(builder.cohortId("C").description("Cohort C").build());
        configs.add(builder.cohortId("D").description("Cohort D").build());
        configs.add(builder.cohortId("E").description("Cohort E").build());
        configs.add(builder.cohortId("F").description("Cohort F").build());

        return configs;
    }

    @NotNull
    private static List<InclusionCriteriaConfig> createTestInclusionCriteriaConfigs() {
        List<InclusionCriteriaConfig> configs = Lists.newArrayList();

        ImmutableInclusionCriteriaConfig.Builder builder = ImmutableInclusionCriteriaConfig.builder().trialId(TEST_TRIAL_ID);

        configs.add(builder.eligibilityRule(EligibilityRule.IS_ADULT).build());

        return configs;
    }
}
