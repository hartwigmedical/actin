package com.hartwig.actin.treatment.trial;

import java.util.List;

import com.google.common.collect.Lists;
import com.hartwig.actin.treatment.datamodel.EligibilityRule;
import com.hartwig.actin.treatment.trial.config.CohortDefinitionConfig;
import com.hartwig.actin.treatment.trial.config.ImmutableCohortDefinitionConfig;
import com.hartwig.actin.treatment.trial.config.ImmutableInclusionCriteriaConfig;
import com.hartwig.actin.treatment.trial.config.ImmutableTrialDefinitionConfig;
import com.hartwig.actin.treatment.trial.config.InclusionCriteriaConfig;
import com.hartwig.actin.treatment.trial.config.TrialDefinitionConfig;

import org.jetbrains.annotations.NotNull;

public final class TestTrialConfigFactory {

    static final String TEST_TRIAL_ID = "TEST";

    private TestTrialConfigFactory() {
    }

    @NotNull
    public static TrialConfigModel createMinimalTestTrialConfigModel() {
        return new TrialConfigModel(createMinimalTestTrialConfigDatabase());
    }

    @NotNull
    public static TrialConfigModel createProperTestTrialConfigModel() {
        return new TrialConfigModel(createProperTestTrialConfigDatabase());
    }

    @NotNull
    public static TrialConfigDatabase createMinimalTestTrialConfigDatabase() {
        return ImmutableTrialConfigDatabase.builder().build();
    }

    @NotNull
    public static TrialConfigDatabase createProperTestTrialConfigDatabase() {
        return ImmutableTrialConfigDatabase.builder()
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

        return configs;
    }

    @NotNull
    private static List<InclusionCriteriaConfig> createTestInclusionCriteriaConfigs() {
        List<InclusionCriteriaConfig> configs = Lists.newArrayList();

        ImmutableInclusionCriteriaConfig.Builder builder = ImmutableInclusionCriteriaConfig.builder().trialId(TEST_TRIAL_ID);

        configs.add(builder.eligibilityRule(EligibilityRule.IS_ADULT).build());
        configs.add(builder.eligibilityRule(EligibilityRule.IS_ADULT).addAppliesToCohorts("A").build());

        return configs;
    }
}
