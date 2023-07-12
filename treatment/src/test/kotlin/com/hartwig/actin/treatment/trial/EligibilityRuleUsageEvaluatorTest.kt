package com.hartwig.actin.treatment.trial

import com.hartwig.actin.treatment.datamodel.TestTreatmentFactory
import com.hartwig.actin.treatment.trial.EligibilityRuleUsageEvaluator.evaluate
import org.junit.Test

class EligibilityRuleUsageEvaluatorTest {

    @Test
    fun canEvaluateEligibilityRuleUsage() {
        val trials = listOf(TestTreatmentFactory.createMinimalTestTrial(), TestTreatmentFactory.createProperTestTrial())
        evaluate(trials)
    }
}