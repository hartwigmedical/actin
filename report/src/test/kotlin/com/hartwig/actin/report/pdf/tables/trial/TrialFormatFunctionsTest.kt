package com.hartwig.actin.report.pdf.tables.trial

import org.assertj.core.api.Assertions.assertThat
import org.junit.Test

class TrialFormatFunctionsTest {

    @Test
    fun `Should format cohorts from trials string correctly if cohort count more than 0 and different total trial count`() {
        assertThat(TrialFormatFunctions.generateCohortsFromTrialsString(cohortCount = 4, trialCount = 2)).isEqualTo("(4 cohorts from 2 trials)")
    }

    @Test
    fun `Should format cohorts from trials string correctly if cohort count more than 0 and equal total trial count`() {
        assertThat(TrialFormatFunctions.generateCohortsFromTrialsString(cohortCount = 2, trialCount = 2)).isEqualTo("(2 trials)")
    }

    @Test
    fun `Should format cohorts from trials string correctly if cohort is 0`() {
        assertThat(TrialFormatFunctions.generateCohortsFromTrialsString(cohortCount = 0, trialCount = 0)).isEqualTo("(0 trials)")
    }

    @Test(expected = IllegalStateException::class)
    fun `Should throw exception if trial count is more than cohort count`() {
        TrialFormatFunctions.generateCohortsFromTrialsString(cohortCount = 0, trialCount = 1)
    }
}