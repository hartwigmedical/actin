package com.hartwig.actin.treatment.util

import com.hartwig.actin.treatment.datamodel.TestTrialFactory
import com.hartwig.actin.treatment.util.EligibilityFunctionDisplay.format
import org.junit.Assert
import org.junit.Test

class EligibilityFunctionDisplayTest {
    @Test
    fun canFormatAllTestFunctions() {
        val trial = TestTrialFactory.createProperTestTrial()
        for (eligibility in trial.generalEligibility()) {
            Assert.assertNotNull(format(eligibility.function()))
        }
    }
}