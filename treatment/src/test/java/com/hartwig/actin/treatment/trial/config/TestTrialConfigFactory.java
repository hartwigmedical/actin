package com.hartwig.actin.treatment.trial.config;

import java.util.List;
import java.util.Set;

import com.google.common.collect.Lists;
import com.google.common.collect.Sets;
import com.hartwig.actin.treatment.TestTrialData;
import com.hartwig.actin.treatment.datamodel.EligibilityRule;
import com.hartwig.actin.treatment.trial.ImmutableTrialConfigDatabase;
import com.hartwig.actin.treatment.trial.TrialConfigDatabase;

import org.jetbrains.annotations.NotNull;

public final class TestTrialConfigFactory {

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
                .trialId(TestTrialData.TEST_TRIAL_ID_1)
                .open(true)
                .acronym("Acronym-" + TestTrialData.TEST_TRIAL_ID_1)
                .title("Title for " + TestTrialData.TEST_TRIAL_ID_1)
                .build());

        configs.add(ImmutableTrialDefinitionConfig.builder()
                .trialId(TestTrialData.TEST_TRIAL_ID_2)
                .open(true)
                .acronym("Acronym-" + TestTrialData.TEST_TRIAL_ID_2)
                .title("Title for " + TestTrialData.TEST_TRIAL_ID_2)
                .build());

        return configs;
    }

    @NotNull
    private static List<CohortDefinitionConfig> createTestCohortDefinitionConfigs() {
        List<CohortDefinitionConfig> configs = Lists.newArrayList();

        ImmutableCohortDefinitionConfig.Builder builder = ImmutableCohortDefinitionConfig.builder().trialId(TestTrialData.TEST_TRIAL_ID_1);

        configs.add(builder.cohortId("A").ctcCohortIds(Set.of("1", "2")).evaluable(true).blacklist(false).description("Cohort A").build());
        configs.add(builder.cohortId("B")
                .ctcCohortIds(Set.of("NA"))
                .evaluable(true)
                .open(true)
                .slotsAvailable(false)
                .blacklist(true)
                .description("Cohort B")
                .build());

        configs.add(builder.cohortId("C")
                .ctcCohortIds(Set.of("wont_be_mapped_because_closed"))
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

        ImmutableInclusionCriteriaConfig.Builder builder =
                ImmutableInclusionCriteriaConfig.builder().trialId(TestTrialData.TEST_TRIAL_ID_1);

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

        ImmutableInclusionCriteriaConfig.Builder builder =
                ImmutableInclusionCriteriaConfig.builder().trialId(TestTrialData.TEST_TRIAL_ID_2);

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
                ImmutableInclusionCriteriaReferenceConfig.builder().trialId(TestTrialData.TEST_TRIAL_ID_1);

        configs.add(builder.referenceId("I-01").referenceText("Should be an adult").build());
        configs.add(builder.referenceId("I-02").referenceText("Should be tested in the lab").build());
        configs.add(builder.referenceId("I-03").referenceText("Should not have any serious other conditions").build());

        return configs;
    }

    @NotNull
    private static List<InclusionCriteriaReferenceConfig> createTestInclusionCriteriaReferenceConfigsForTestTrial2() {
        List<InclusionCriteriaReferenceConfig> configs = Lists.newArrayList();

        ImmutableInclusionCriteriaReferenceConfig.Builder builder =
                ImmutableInclusionCriteriaReferenceConfig.builder().trialId(TestTrialData.TEST_TRIAL_ID_2);

        configs.add(builder.referenceId("I-01").referenceText("Should be an adult").build());

        return configs;
    }

}
