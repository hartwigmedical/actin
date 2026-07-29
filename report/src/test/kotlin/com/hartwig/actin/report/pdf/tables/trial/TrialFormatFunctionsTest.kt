package com.hartwig.actin.report.pdf.tables.trial

import com.hartwig.actin.configuration.ReportIntendedUse
import com.hartwig.actin.report.pdf.ReportLabels
import org.assertj.core.api.Assertions.assertThat
import org.junit.jupiter.api.Assertions.assertThrows
import org.junit.jupiter.api.Test

class TrialFormatFunctionsTest {

    private val labels = ReportLabels.load(ReportIntendedUse.RESEARCH_USE_ONLY)

    @Test
    fun `Should format cohorts from trials string correctly if cohort count more than 0 and different total trial count`() {
        assertThat(TrialFormatFunctions.generateCohortsFromTrialsString(cohortCount = 4, trialCount = 2, labels = labels)).isEqualTo("(4 cohorts from 2 trials)")
    }

    @Test
    fun `Should format cohorts from trials string correctly if cohort count more than 0 and equal total trial count`() {
        assertThat(TrialFormatFunctions.generateCohortsFromTrialsString(cohortCount = 2, trialCount = 2, labels = labels)).isEqualTo("(2 trials)")
    }

    @Test
    fun `Should format cohorts from trials string correctly if cohort is 0`() {
        assertThat(TrialFormatFunctions.generateCohortsFromTrialsString(cohortCount = 0, trialCount = 0, labels = labels)).isEqualTo("(0 trials)")
    }

    @Test
    fun `Should throw exception if trial count is more than cohort count`() {
        assertThrows(IllegalStateException::class.java) {
            TrialFormatFunctions.generateCohortsFromTrialsString(cohortCount = 0, trialCount = 1, labels = labels)
        }
    }
}