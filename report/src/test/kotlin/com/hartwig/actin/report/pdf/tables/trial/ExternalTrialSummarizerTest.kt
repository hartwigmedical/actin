package com.hartwig.actin.report.pdf.tables.trial

import com.hartwig.actin.molecular.datamodel.evidence.Country
import com.hartwig.actin.molecular.datamodel.evidence.ExternalTrial
import com.hartwig.actin.report.interpretation.EvaluatedCohort
import org.assertj.core.api.Assertions.assertThat
import org.junit.Test

private const val TMB_TARGET = "TMB"
private const val EGFR_TARGET = "EGFR"
private val TRIAL_1 = ExternalTrial("1", setOf(Country.NETHERLANDS), "url", "NCT001")
private val TRIAL_2 = ExternalTrial("2", setOf(Country.BELGIUM), "url", "NCT002")

class ExternalTrialSummarizerTest {

    private val externalTrialSummarizer = ExternalTrialSummarizer(true)

    @Test
    fun `Should not filter any external trials when no molecular targets overlap`() {
        val externalEligibleTrials =
            mapOf(
                TMB_TARGET to listOf(TRIAL_1),
                EGFR_TARGET to listOf(TRIAL_2)
            )
        val externalTrialSummary =
            externalTrialSummarizer.summarize(externalEligibleTrials, emptyList())
        assertThat(externalTrialSummary.dutchTrials).containsOnlyKeys(TMB_TARGET)
        assertThat(externalTrialSummary.otherCountryTrials).containsOnlyKeys(EGFR_TARGET)
    }

    @Test
    fun `Should filter dutch and other external trials when molecular targets included in hospital local trials`() {
        val externalEligibleTrials =
            mapOf(
                TMB_TARGET to listOf(TRIAL_1, TRIAL_2),
            )
        val externalTrialSummary =
            externalTrialSummarizer.summarize(externalEligibleTrials, listOf(evaluatedCohortTMB()))
        assertThat(externalTrialSummary.dutchTrials).isEmpty()
        assertThat(externalTrialSummary.otherCountryTrials).isEmpty()
    }

    @Test
    fun `Should filter other country trials when molecular targets included in dutch trials`() {
        val externalEligibleTrials =
            mapOf(
                TMB_TARGET to listOf(TRIAL_1, TRIAL_2),
            )
        val externalTrialSummary =
            externalTrialSummarizer.summarize(externalEligibleTrials, emptyList())
        assertThat(externalTrialSummary.dutchTrials).containsOnlyKeys(TMB_TARGET)
        assertThat(externalTrialSummary.otherCountryTrials).isEmpty()
    }

    @Test
    fun `Should filter when trial has multiple targets and one does not overlap`() {
        val externalEligibleTrials =
            mapOf(
                TMB_TARGET to listOf(TRIAL_1, TRIAL_2),
                EGFR_TARGET to listOf(TRIAL_2)
            )
        val externalTrialSummary =
            externalTrialSummarizer.summarize(externalEligibleTrials, emptyList())
        assertThat(externalTrialSummary.dutchTrials).containsOnlyKeys(TMB_TARGET)
        assertThat(externalTrialSummary.otherCountryTrials).containsOnlyKeys(EGFR_TARGET)
    }

    @Test
    fun `Should disable filtering when toggle is disabled`() {
        val externalEligibleTrials =
            mapOf(
                TMB_TARGET to listOf(TRIAL_1, TRIAL_2),
            )
        val externalTrialSummary =
            ExternalTrialSummarizer(false).summarize(externalEligibleTrials, listOf(evaluatedCohortTMB()))
        assertThat(externalTrialSummary.dutchTrials).containsOnlyKeys(TMB_TARGET)
        assertThat(externalTrialSummary.otherCountryTrials).containsOnlyKeys(TMB_TARGET)
    }

    private fun evaluatedCohortTMB() =
        EvaluatedCohort(
            trialId = "id",
            acronym = "acronym",
            cohort = null,
            molecularEvents = setOf(TMB_TARGET),
            isPotentiallyEligible = true,
            isOpen = true,
            hasSlotsAvailable = true,
            warnings = emptySet(),
            fails = emptySet()
        )
}