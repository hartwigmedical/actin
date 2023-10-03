package com.hartwig.actin.trial.trial

import com.hartwig.actin.treatment.datamodel.TestTreatmentFactory
import org.junit.Test

class EligibilityRuleUsageEvaluatorTest {

    @Test
    fun canEvaluateEligibilityRuleUsage() {
        val trials = listOf(TestTreatmentFactory.createMinimalTestTrial(), TestTreatmentFactory.createProperTestTrial())
        EligibilityRuleUsageEvaluator.evaluate(trials)
    }
}