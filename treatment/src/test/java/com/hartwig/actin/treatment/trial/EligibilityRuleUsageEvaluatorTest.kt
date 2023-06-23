package com.hartwig.actin.treatment.trial

import com.google.common.collect.Lists
import com.hartwig.actin.treatment.datamodel.TestTreatmentFactory
import com.hartwig.actin.treatment.datamodel.Trial
import com.hartwig.actin.treatment.trial.EligibilityRuleUsageEvaluator.evaluate
import org.junit.Test

class EligibilityRuleUsageEvaluatorTest {
    @Test
    fun canEvaluateEligibilityRuleUsage() {
        val trials: List<Trial?> =
            Lists.newArrayList(TestTreatmentFactory.createMinimalTestTrial(), TestTreatmentFactory.createProperTestTrial())
        evaluate(trials)
    }
}