package com.hartwig.actin.report.pdf.tables.trial

import com.hartwig.actin.algo.datamodel.TrialMatch
import com.hartwig.actin.molecular.datamodel.evidence.Country
import com.hartwig.actin.molecular.datamodel.evidence.CountryName
import com.hartwig.actin.molecular.datamodel.evidence.TestExternalTrialFactory
import com.hartwig.actin.report.interpretation.EvaluatedCohort
import com.hartwig.actin.trial.datamodel.TrialIdentification
import org.assertj.core.api.Assertions.assertThat
import org.junit.Test

private const val TMB_TARGET = "TMB"
private const val EGFR_TARGET = "EGFR"
private val TRIAL_1 = TestExternalTrialFactory.create(
    "1", setOf(Country(CountryName.NETHERLANDS, emptyMap())), "url", "NCT001"
)
private val TRIAL_2 = TestExternalTrialFactory.create(
    "2", setOf(Country(CountryName.BELGIUM, emptyMap())), "url", "NCT002"
)
private val trialMatches = listOf(
    TrialMatch(
        identification = TrialIdentification("TRIAL-1", true, "TR-1", "Different title of same trial 1", "NCT00000001"),
        isPotentiallyEligible = true,
        evaluations = emptyMap(),
        cohorts = emptyList()
    ),
    TrialMatch(
        identification = TrialIdentification("TRIAL-2", true, "TR-2", "Different trial 2", "NCT00000003"),
        isPotentiallyEligible = true,
        evaluations = emptyMap(),
        cohorts = emptyList()
    )
)

class ExternalTrialSummarizerTest {

    private val externalTrialSummarizer = ExternalTrialSummarizer(CountryName.NETHERLANDS)

    @Test
    fun `Should correctly group trials with identical nctIds combining all events of these trials`() {
        val externalTrialTargetingTwoEvents = externalTrial(5)
        val externalTrialTargetingOneEvent = externalTrial(6)
        val externalTrialsPerEvent = mapOf(
            "event1" to listOf(externalTrialTargetingTwoEvents),
            "event2" to listOf(externalTrialTargetingOneEvent),
            "event3" to listOf(externalTrialTargetingTwoEvents)
        )

        assertThat(externalTrialSummarizer.filterAndGroupExternalTrialsByNctIdAndEvents(externalTrialsPerEvent, trialMatches)).isEqualTo(
            mapOf(
                "event1,\nevent3" to listOf(externalTrialTargetingTwoEvents),
                "event2" to listOf(externalTrialTargetingOneEvent),
            )
        )
    }

    @Test
    fun `Should filter out external trials with NCT ID that matches local trial and maintain event to trial mapping`() {
        val externalTrialWithMatchToLocal = externalTrial(1)
        val externalTrialWithoutMatchToLocal = externalTrial(2)
        val externalTrialsPerEvent = mapOf(
            "event1" to listOf(externalTrialWithMatchToLocal, externalTrialWithoutMatchToLocal)
        )
        assertThat(externalTrialSummarizer.filterAndGroupExternalTrialsByNctIdAndEvents(externalTrialsPerEvent, trialMatches)).isEqualTo(
            mapOf(
                "event1" to listOf(externalTrialWithoutMatchToLocal)
            )
        )
    }

    @Test
    fun `Should return unchanged external trial map when trialMatches is empty`() {
        val externalTrialsPerEvent = mapOf("event1" to listOf(externalTrial(1), externalTrial(2)))
        assertThat(externalTrialSummarizer.filterAndGroupExternalTrialsByNctIdAndEvents(externalTrialsPerEvent, emptyList())).isEqualTo(
            externalTrialsPerEvent
        )
    }

    @Test
    fun `Should not filter any external trials when no molecular targets overlap`() {
        val externalEligibleTrials =
            mapOf(
                TMB_TARGET to listOf(TRIAL_1),
                EGFR_TARGET to listOf(TRIAL_2)
            )
        val externalTrialSummary =
            externalTrialSummarizer.filterMolecularCriteriaAlreadyPresent(externalEligibleTrials, emptyList())
        assertThat(externalTrialSummary.localTrials).containsOnlyKeys(TMB_TARGET)
        assertThat(externalTrialSummary.nonLocalTrials).containsOnlyKeys(EGFR_TARGET)
        assertThat(externalTrialSummary.localTrialsFiltered).isEqualTo(0)
        assertThat(externalTrialSummary.nonLocalTrialsFiltered).isEqualTo(0)
    }

    @Test
    fun `Should filter dutch and other external trials when molecular targets included in hospital local trials`() {
        val externalEligibleTrials =
            mapOf(
                TMB_TARGET to listOf(TRIAL_1, TRIAL_2),
            )
        val externalTrialSummary =
            externalTrialSummarizer.filterMolecularCriteriaAlreadyPresent(externalEligibleTrials, listOf(evaluatedCohortTMB()))
        assertThat(externalTrialSummary.localTrials).isEmpty()
        assertThat(externalTrialSummary.nonLocalTrials).isEmpty()
        assertThat(externalTrialSummary.localTrialsFiltered).isEqualTo(1)
        assertThat(externalTrialSummary.nonLocalTrialsFiltered).isEqualTo(1)
    }

    @Test
    fun `Should filter other country trials when molecular targets included in dutch trials`() {
        val externalEligibleTrials =
            mapOf(
                TMB_TARGET to listOf(TRIAL_1, TRIAL_2),
            )
        val externalTrialSummary =
            externalTrialSummarizer.filterMolecularCriteriaAlreadyPresent(externalEligibleTrials, emptyList())
        assertThat(externalTrialSummary.localTrials).containsOnlyKeys(TMB_TARGET)
        assertThat(externalTrialSummary.nonLocalTrials).isEmpty()
        assertThat(externalTrialSummary.localTrialsFiltered).isEqualTo(0)
        assertThat(externalTrialSummary.nonLocalTrialsFiltered).isEqualTo(1)
    }

    @Test
    fun `Should not filter when trial has multiple targets and one does not overlap`() {
        val externalEligibleTrials =
            mapOf(
                TMB_TARGET to listOf(TRIAL_1, TRIAL_2),
                EGFR_TARGET to listOf(TRIAL_2)
            )
        val externalTrialSummary =
            externalTrialSummarizer.filterMolecularCriteriaAlreadyPresent(externalEligibleTrials, emptyList())
        assertThat(externalTrialSummary.localTrials).containsOnlyKeys(TMB_TARGET)
        assertThat(externalTrialSummary.nonLocalTrials).containsOnlyKeys(EGFR_TARGET)
        assertThat(externalTrialSummary.localTrialsFiltered).isEqualTo(0)
        assertThat(externalTrialSummary.nonLocalTrialsFiltered).isEqualTo(0)
    }

    @Test
    fun `Should handle trials with combined targets`() {
        val combinedTarget = "$TMB_TARGET,\n$EGFR_TARGET"
        val externalEligibleTrials =
            mapOf(
                combinedTarget to listOf(TRIAL_1, TRIAL_2),
                EGFR_TARGET to listOf(TRIAL_2)
            )
        val externalTrialSummary =
            externalTrialSummarizer.filterMolecularCriteriaAlreadyPresent(externalEligibleTrials, emptyList())
        assertThat(externalTrialSummary.localTrials).containsOnlyKeys(combinedTarget)
        assertThat(externalTrialSummary.nonLocalTrials).isEmpty()
        assertThat(externalTrialSummary.localTrialsFiltered).isEqualTo(0)
        assertThat(externalTrialSummary.nonLocalTrialsFiltered).isEqualTo(1)
    }

    private fun externalTrial(id: Int) =
        TestExternalTrialFactory.create(
            "Title of trial $id",
            setOf(Country(CountryName.NETHERLANDS, emptyMap()), Country(CountryName.BELGIUM, emptyMap())),
            "url",
            "NCT0000000$id"
        )

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