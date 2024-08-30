package com.hartwig.actin.trial.util

import com.hartwig.actin.datamodel.trial.TestTrialFactory
import com.hartwig.actin.trial.util.EligibilityFunctionDisplay.format
import org.assertj.core.api.Assertions.assertThat
import org.junit.Test

class EligibilityFunctionDisplayTest {

    @Test
    fun `Should format all test functions`() {
        val trial = TestTrialFactory.createProperTestTrial()
        for (eligibility in trial.generalEligibility) {
            assertThat(format(eligibility.function)).isNotNull()
        }
    }
}