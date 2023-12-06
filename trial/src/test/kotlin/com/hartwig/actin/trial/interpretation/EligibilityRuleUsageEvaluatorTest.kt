package com.hartwig.actin.trial.interpretation

import com.hartwig.actin.treatment.datamodel.TestTrialFactory
import org.junit.Test

class EligibilityRuleUsageEvaluatorTest {

    @Test
    fun canEvaluateEligibilityRuleUsage() {
        val trials = listOf(TestTrialFactory.createMinimalTestTrial(), TestTrialFactory.createProperTestTrial())
        EligibilityRuleUsageEvaluator.evaluate(trials)
    }
}