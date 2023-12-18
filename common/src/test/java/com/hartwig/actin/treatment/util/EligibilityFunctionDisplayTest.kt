package com.hartwig.actin.treatment.util

import com.hartwig.actin.treatment.datamodel.TestTrialFactory
import com.hartwig.actin.treatment.util.EligibilityFunctionDisplay.format
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