package com.hartwig.actin.report.pdf.tables.trial

import org.assertj.core.api.Assertions.assertThat
import org.junit.Test

class TrialFormatFunctionsTest {

    @Test
    fun `Should format cohorts from trials string correctly if cohort count more than 0 and different total trial count`() {
        val cohortCount = 4
        val trialCount = 2
        assertThat(TrialFormatFunctions.generateCohortsFromTrialsString(cohortCount, trialCount)).isEqualTo("(4 cohorts from 2 trials)")
    }

    @Test
    fun `Should format cohorts from trials string correctly if cohort count more than 0 and equal total trial count`() {
        val cohortCount = 2
        val trialCount = 2
        assertThat(TrialFormatFunctions.generateCohortsFromTrialsString(cohortCount, trialCount)).isEqualTo("(2 trials)")
    }

    @Test
    fun `Should format cohorts from trials string correctly if cohort is 0`() {
        val cohortCount = 0
        val trialCount = 0
        assertThat(TrialFormatFunctions.generateCohortsFromTrialsString(cohortCount, trialCount)).isEqualTo("(0 trials)")
    }

    @Test(expected = IllegalStateException::class)
    fun `Should throw exception if trial count is more than cohort count`() {
        val cohortCount = 0
        val trialCount = 1
        TrialFormatFunctions.generateCohortsFromTrialsString(cohortCount, trialCount)
    }
}