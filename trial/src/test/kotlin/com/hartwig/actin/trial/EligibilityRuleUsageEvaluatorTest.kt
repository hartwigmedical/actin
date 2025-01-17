package com.hartwig.actin.trial

import com.hartwig.actin.datamodel.trial.EligibilityRule
import com.hartwig.actin.datamodel.trial.TestTrialFactory
import org.assertj.core.api.Assertions.assertThat
import org.junit.Test

class EligibilityRuleUsageEvaluatorTest {

    @Test
    fun `Should evaluate eligibility rule usage`() {
        val trials = listOf(TestTrialFactory.createMinimalTestTrial(), TestTrialFactory.createProperTestTrial())
        val expectedUnusedRule = EligibilityRule.HAS_HAD_TREATMENT_WITH_ANY_DRUG_X
        val expectedUsedRules = setOf(
            EligibilityRule.IS_AT_LEAST_X_YEARS_OLD,
            EligibilityRule.HAS_EXHAUSTED_SOC_TREATMENTS,
            EligibilityRule.HAS_KNOWN_ACTIVE_CNS_METASTASES,
            EligibilityRule.NOT
        )
        val unusedRulesToKeep = EligibilityRule.values().toSet() - expectedUnusedRule - expectedUsedRules
        assertThat(EligibilityRuleUsageEvaluator.evaluate(trials, unusedRulesToKeep)).containsExactly(expectedUnusedRule)
    }
}