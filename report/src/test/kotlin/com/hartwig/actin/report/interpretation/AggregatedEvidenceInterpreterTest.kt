package com.hartwig.actin.report.interpretation

import com.hartwig.actin.algo.datamodel.TrialMatch
import com.hartwig.actin.molecular.datamodel.evidence.Country
import com.hartwig.actin.molecular.datamodel.evidence.ExternalTrial
import com.hartwig.actin.report.interpretation.AggregatedEvidenceInterpreter.filterAndGroupExternalTrialsByNctIdAndEvents
import com.hartwig.actin.trial.datamodel.TrialIdentification
import org.assertj.core.api.Assertions.assertThat
import org.junit.Test

class AggregatedEvidenceInterpreterTest {

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

    @Test
    fun `Should correctly group trials with identical nctIds combining all events of these trials`() {
        val externalTrialTargetingTwoEvents = externalTrial(5)
        val externalTrialTargetingOneEvent = externalTrial(6)
        val externalTrialsPerEvent = mapOf(
            "event1" to listOf(externalTrialTargetingTwoEvents),
            "event2" to listOf(externalTrialTargetingOneEvent),
            "event3" to listOf(externalTrialTargetingTwoEvents)
        )

        assertThat(filterAndGroupExternalTrialsByNctIdAndEvents(externalTrialsPerEvent, trialMatches)).isEqualTo(
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
        assertThat(filterAndGroupExternalTrialsByNctIdAndEvents(externalTrialsPerEvent, trialMatches)).isEqualTo(
            mapOf(
                "event1" to listOf(externalTrialWithoutMatchToLocal)
            )
        )
    }

    @Test
    fun `Should return unchanged external trial map when trialMatches is empty`() {
        val externalTrialsPerEvent = mapOf("event1" to listOf(externalTrial(1), externalTrial(2)))
        assertThat(filterAndGroupExternalTrialsByNctIdAndEvents(externalTrialsPerEvent, emptyList())).isEqualTo(externalTrialsPerEvent)
    }

    private fun externalTrial(id: Int) =
        ExternalTrial("Title of trial $id", setOf(Country.NETHERLANDS, Country.BELGIUM), "url", "NCT0000000$id")
}