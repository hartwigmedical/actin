package com.hartwig.actin.treatment.trial.config;

import java.util.List;

import com.google.common.collect.Lists;
import com.google.common.collect.Sets;
import com.hartwig.actin.treatment.datamodel.EligibilityRule;
import com.hartwig.actin.treatment.trial.ImmutableTrialConfigDatabase;
import com.hartwig.actin.treatment.trial.TrialConfigDatabase;

import org.jetbrains.annotations.NotNull;

public final class TestTrialConfigFactory {

    public static final String TEST_TRIAL_ID_1 = "TEST-1";
    public static final String TEST_TRIAL_ID_2 = "TEST-2";

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
                .inclusionCriteriaReferenceConfigs(createTestInclusionCriteriaReferenceConfigs())
                .build();
    }

    @NotNull
    private static List<TrialDefinitionConfig> createTestTrialDefinitionConfigs() {
        List<TrialDefinitionConfig> configs = Lists.newArrayList();

        configs.add(ImmutableTrialDefinitionConfig.builder()
                .trialId(TEST_TRIAL_ID_1)
                .open(true)
                .acronym("Acronym-" + TEST_TRIAL_ID_1)
                .title("Title for " + TEST_TRIAL_ID_1)
                .build());

        configs.add(ImmutableTrialDefinitionConfig.builder()
                .trialId(TEST_TRIAL_ID_2)
                .open(true)
                .acronym("Acronym-" + TEST_TRIAL_ID_2)
                .title("Title for " + TEST_TRIAL_ID_2)
                .build());

        return configs;
    }

    @NotNull
    private static List<CohortDefinitionConfig> createTestCohortDefinitionConfigs() {
        List<CohortDefinitionConfig> configs = Lists.newArrayList();

        ImmutableCohortDefinitionConfig.Builder builder = ImmutableCohortDefinitionConfig.builder().trialId(TEST_TRIAL_ID_1);

        configs.add(builder.cohortId("A").evaluable(true).open(true).slotsAvailable(true).blacklist(false).description("Cohort A").build());
        configs.add(builder.cohortId("B").evaluable(true).open(true).slotsAvailable(false).blacklist(true).description("Cohort B").build());

        configs.add(builder.cohortId("C")
                .evaluable(false)
                .open(false)
                .slotsAvailable(false)
                .blacklist(false)
                .description("Cohort C")
                .build());

        return configs;
    }

    @NotNull
    private static List<InclusionCriteriaConfig> createTestInclusionCriteriaConfigs() {
        List<InclusionCriteriaConfig> configs = Lists.newArrayList();

        configs.addAll(createTestInclusionCriteriaForTestTrial1());
        configs.addAll(createTestInclusionCriteriaForTestTrial2());

        return configs;
    }

    private static List<InclusionCriteriaConfig> createTestInclusionCriteriaForTestTrial1() {
        List<InclusionCriteriaConfig> configs = Lists.newArrayList();

        ImmutableInclusionCriteriaConfig.Builder builder = ImmutableInclusionCriteriaConfig.builder().trialId(TEST_TRIAL_ID_1);

        configs.add(builder.referenceIds(Sets.newHashSet("I-01")).inclusionRule(EligibilityRule.IS_AT_LEAST_X_YEARS_OLD + "[18]").build());
        configs.add(builder.referenceIds(Sets.newHashSet("I-02"))
                .inclusionRule(EligibilityRule.HAS_INR_ULN_OF_AT_MOST_X + "[1]")
                .addAppliesToCohorts("A")
                .build());

        String rule1 = EligibilityRule.HAS_ACTIVE_INFECTION.toString();
        String rule2 = EligibilityRule.HAS_SEVERE_CONCOMITANT_CONDITION.toString();

        configs.add(builder.referenceIds(Sets.newHashSet("I-03"))
                .inclusionRule("NOT(OR(" + rule1 + ", " + rule2 + "))")
                .addAppliesToCohorts("A")
                .build());

        return configs;
    }

    private static List<InclusionCriteriaConfig> createTestInclusionCriteriaForTestTrial2() {
        List<InclusionCriteriaConfig> configs = Lists.newArrayList();

        ImmutableInclusionCriteriaConfig.Builder builder = ImmutableInclusionCriteriaConfig.builder().trialId(TEST_TRIAL_ID_2);

        configs.add(builder.referenceIds(Sets.newHashSet("I-01")).inclusionRule(EligibilityRule.IS_AT_LEAST_X_YEARS_OLD + "[18]").build());

        return configs;
    }

    @NotNull
    private static List<InclusionCriteriaReferenceConfig> createTestInclusionCriteriaReferenceConfigs() {
        List<InclusionCriteriaReferenceConfig> configs = Lists.newArrayList();

        configs.addAll(createTestInclusionCriteriaReferenceConfigsForTestTrial1());
        configs.addAll(createTestInclusionCriteriaReferenceConfigsForTestTrial2());

        return configs;
    }

    @NotNull
    private static List<InclusionCriteriaReferenceConfig> createTestInclusionCriteriaReferenceConfigsForTestTrial1() {
        List<InclusionCriteriaReferenceConfig> configs = Lists.newArrayList();

        ImmutableInclusionCriteriaReferenceConfig.Builder builder =
                ImmutableInclusionCriteriaReferenceConfig.builder().trialId(TEST_TRIAL_ID_1);

        configs.add(builder.referenceId("I-01").referenceText("Should be an adult").build());
        configs.add(builder.referenceId("I-02").referenceText("Should be tested in the lab").build());
        configs.add(builder.referenceId("I-03").referenceText("Should not have any serious other conditions").build());

        return configs;
    }

    @NotNull
    private static List<InclusionCriteriaReferenceConfig> createTestInclusionCriteriaReferenceConfigsForTestTrial2() {
        List<InclusionCriteriaReferenceConfig> configs = Lists.newArrayList();

        ImmutableInclusionCriteriaReferenceConfig.Builder builder =
                ImmutableInclusionCriteriaReferenceConfig.builder().trialId(TEST_TRIAL_ID_2);

        configs.add(builder.referenceId("I-01").referenceText("Should be an adult").build());

        return configs;
    }

}
