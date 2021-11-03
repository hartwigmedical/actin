package com.hartwig.actin.treatment.trial.config;

import java.util.List;

import com.google.common.collect.Lists;
import com.hartwig.actin.treatment.datamodel.EligibilityRule;
import com.hartwig.actin.treatment.trial.ImmutableTrialConfigDatabase;
import com.hartwig.actin.treatment.trial.TrialConfigDatabase;

import org.apache.logging.log4j.util.Strings;
import org.jetbrains.annotations.NotNull;

public final class TestTrialConfigFactory {

    public static final String TEST_TRIAL_ID = "TEST";

    private TestTrialConfigFactory() {
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

        ImmutableCohortDefinitionConfig.Builder builder = ImmutableCohortDefinitionConfig.builder().trialId(TEST_TRIAL_ID);

        configs.add(builder.cohortId("A").open(true).description("Cohort A").build());
        configs.add(builder.cohortId("B").open(true).description("Cohort B").build());
        configs.add(builder.cohortId("C").open(false).description("Cohort C").build());

        return configs;
    }

    @NotNull
    private static List<InclusionCriteriaConfig> createTestInclusionCriteriaConfigs() {
        List<InclusionCriteriaConfig> configs = Lists.newArrayList();

        ImmutableInclusionCriteriaConfig.Builder builder =
                ImmutableInclusionCriteriaConfig.builder().trialId(TEST_TRIAL_ID).reference(Strings.EMPTY).description(Strings.EMPTY);

        configs.add(builder.inclusionCriterion(EligibilityRule.IS_AT_LEAST_18_YEARS_OLD.toString()).build());
        configs.add(builder.inclusionCriterion(EligibilityRule.HAS_INR_ULN_AT_MOST_X + "[1]").addAppliesToCohorts("A").build());

        configs.add(builder.inclusionCriterion(
                "NOT(OR(" + EligibilityRule.HAS_ACTIVE_INFECTION + ", " + EligibilityRule.HAS_SIGNIFICANT_CONCOMITANT_ILLNESS + "))")
                .addAppliesToCohorts("A")
                .build());

        return configs;
    }
}
