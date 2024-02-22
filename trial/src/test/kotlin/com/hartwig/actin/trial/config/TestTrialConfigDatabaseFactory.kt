package com.hartwig.actin.trial.config

import com.google.common.collect.Sets
import com.hartwig.actin.trial.TestTrialData
import com.hartwig.actin.trial.datamodel.EligibilityRule

object TestTrialConfigDatabaseFactory {

    fun createMinimalTestTrialConfigDatabase(): TrialConfigDatabase {
        return TrialConfigDatabase(emptyList(), emptyList(), emptyList(), emptyList(), emptyList())
    }

    fun createProperTestTrialConfigDatabase(): TrialConfigDatabase {
        return TrialConfigDatabase(
            trialDefinitionConfigs = createTestTrialDefinitionConfigs(),
            cohortDefinitionConfigs = createTestCohortDefinitionConfigs(),
            inclusionCriteriaConfigs = createTestInclusionCriteriaConfigs(),
            inclusionCriteriaReferenceConfigs = createTestInclusionCriteriaReferenceConfigs(),
            unusedRulesToKeep = EligibilityRule.values().map(EligibilityRule::toString)
        )
    }

    private fun createTestTrialDefinitionConfigs(): List<TrialDefinitionConfig> {
        return listOf(
            TrialDefinitionConfig(
                trialId = TestTrialData.TEST_TRIAL_METC_1,
                open = true,
                acronym = "Acronym-" + TestTrialData.TEST_TRIAL_METC_1,
                title = "Title for " + TestTrialData.TEST_TRIAL_METC_1,
            ),
            TrialDefinitionConfig(
                trialId = TestTrialData.TEST_TRIAL_METC_2,
                open = true,
                acronym = "Acronym-" + TestTrialData.TEST_TRIAL_METC_2,
                title = "Title for " + TestTrialData.TEST_TRIAL_METC_2,
            )
        )
    }

    private fun createTestCohortDefinitionConfigs(): List<CohortDefinitionConfig> {
        return listOf(
            CohortDefinitionConfig(
                trialId = TestTrialData.TEST_TRIAL_METC_1,
                cohortId = "A",
                externalCohortIds = setOf("1", "2"),
                evaluable = true,
                open = null,
                slotsAvailable = null,
                blacklist = false,
                description = "Cohort A"
            ),
            CohortDefinitionConfig(
                trialId = TestTrialData.TEST_TRIAL_METC_1,
                cohortId = "B",
                externalCohortIds = setOf("NA"),
                evaluable = true,
                open = true,
                slotsAvailable = false,
                blacklist = true,
                description = "Cohort B"
            ),
            CohortDefinitionConfig(
                trialId = TestTrialData.TEST_TRIAL_METC_1,
                cohortId = "C",
                externalCohortIds = setOf("wont_be_mapped_because_closed"),
                evaluable = false,
                open = false,
                slotsAvailable = false,
                blacklist = false,
                description = "Cohort C"
            )
        )
    }

    private fun createTestInclusionCriteriaConfigs(): List<InclusionCriteriaConfig> {
        return listOf(createTestInclusionCriteriaForTestTrial1(), createTestInclusionCriteriaForTestTrial2()).flatten()
    }

    private fun createTestInclusionCriteriaForTestTrial1(): List<InclusionCriteriaConfig> {
        val rule1 = EligibilityRule.HAS_ACTIVE_INFECTION.toString()
        val rule2 = EligibilityRule.HAS_SEVERE_CONCOMITANT_CONDITION.toString()
        return listOf(
            InclusionCriteriaConfig(
                trialId = TestTrialData.TEST_TRIAL_METC_1,
                referenceIds = setOf("I-01"),
                appliesToCohorts = emptySet(),
                inclusionRule = EligibilityRule.IS_AT_LEAST_X_YEARS_OLD.toString() + "[18]",
            ), InclusionCriteriaConfig(
                trialId = TestTrialData.TEST_TRIAL_METC_1,
                referenceIds = setOf("I-02"),
                inclusionRule = EligibilityRule.HAS_INR_ULN_OF_AT_MOST_X.toString() + "[1]",
                appliesToCohorts = setOf("A")
            ), InclusionCriteriaConfig(
                trialId = TestTrialData.TEST_TRIAL_METC_1,
                referenceIds = Sets.newHashSet("I-03"),
                inclusionRule = "NOT(OR($rule1, $rule2))",
                appliesToCohorts = setOf("A")
            )
        )
    }

    private fun createTestInclusionCriteriaForTestTrial2(): List<InclusionCriteriaConfig> {
        return listOf(
            InclusionCriteriaConfig(
                trialId = TestTrialData.TEST_TRIAL_METC_2,
                referenceIds = setOf("I-01"),
                appliesToCohorts = emptySet(),
                inclusionRule = EligibilityRule.IS_AT_LEAST_X_YEARS_OLD.toString() + "[18]"
            )
        )
    }

    private fun createTestInclusionCriteriaReferenceConfigs(): List<InclusionCriteriaReferenceConfig> {
        return listOf(
            createTestInclusionCriteriaReferenceConfigsForTestTrial1(), createTestInclusionCriteriaReferenceConfigsForTestTrial2()
        ).flatten()
    }

    private fun createTestInclusionCriteriaReferenceConfigsForTestTrial1(): List<InclusionCriteriaReferenceConfig> {
        return listOf(
            InclusionCriteriaReferenceConfig(
                trialId = TestTrialData.TEST_TRIAL_METC_1,
                referenceId = "I-01",
                referenceText = "Should be an adult"
            ),
            InclusionCriteriaReferenceConfig(
                trialId = TestTrialData.TEST_TRIAL_METC_1,
                referenceId = "I-02",
                referenceText = "Should be tested in the lab"
            ),
            InclusionCriteriaReferenceConfig(
                trialId = TestTrialData.TEST_TRIAL_METC_1,
                referenceId = "I-03",
                referenceText = "Should not have any serious other conditions"
            )
        )
    }

    private fun createTestInclusionCriteriaReferenceConfigsForTestTrial2(): List<InclusionCriteriaReferenceConfig> {
        return listOf(
            InclusionCriteriaReferenceConfig(
                trialId = TestTrialData.TEST_TRIAL_METC_2,
                referenceId = "I-01",
                referenceText = "Should be an adult"
            )
        )
    }
}