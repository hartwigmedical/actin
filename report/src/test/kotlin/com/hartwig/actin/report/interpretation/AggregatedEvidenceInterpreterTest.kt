package com.hartwig.actin.report.interpretation

import com.hartwig.actin.algo.datamodel.TrialMatch
import com.hartwig.actin.molecular.datamodel.evidence.Country
import com.hartwig.actin.molecular.datamodel.evidence.ExternalTrial
import org.assertj.core.api.Assertions.assertThat
import org.junit.Test
import com.hartwig.actin.report.interpretation.AggregatedEvidenceInterpreter.filterAndGroupExternalTrialsByNctIdAndEvents
import com.hartwig.actin.trial.datamodel.TrialIdentification

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
    fun `Should correctly group trials with identical nctIds combining all events of these trials`(){
        val externalTrialTargetingTwoEvents =
            ExternalTrial("Trial targeting event 1 and event 3", setOf(Country.NETHERLANDS, Country.BELGIUM), "url", "NCT00000010")
        val externalTrialTargetingOneEvent =
            ExternalTrial("Trial targeting event 2", setOf(Country.NETHERLANDS, Country.BELGIUM), "url", "NCT00000011")
        val externalTrialsPerEvent = mapOf(
            "event1" to listOf(externalTrialTargetingTwoEvents),
            "event2" to listOf(externalTrialTargetingOneEvent),
            "event3" to listOf(externalTrialTargetingTwoEvents)
        )
        assertThat(filterAndGroupExternalTrialsByNctIdAndEvents(externalTrialsPerEvent, trialMatches)).containsExactlyEntriesOf(mapOf(
            "event1,\nevent3" to listOf(externalTrialTargetingTwoEvents),
            "event2" to listOf(externalTrialTargetingOneEvent),
            )
        )
    }

    @Test
    fun `Should filter out external trials with NCT ID that matches local trial and maintain event to trial mapping`(){
        val externalTrialWithMatchToLocal =
            ExternalTrial("Title of trial 1", setOf(Country.NETHERLANDS, Country.BELGIUM), "url", "NCT00000001")
        val externalTrialWithoutMatchToLocal =
            ExternalTrial("Title of trial 2", setOf(Country.NETHERLANDS, Country.BELGIUM), "url", "NCT00000002")
        val externalTrialsPerEvent = mapOf(
            "event1" to listOf(externalTrialWithMatchToLocal, externalTrialWithoutMatchToLocal)
        )
        assertThat(filterAndGroupExternalTrialsByNctIdAndEvents(externalTrialsPerEvent, trialMatches)).containsExactlyEntriesOf(
            mapOf(
                "event1" to listOf(externalTrialWithoutMatchToLocal)
            )
        )
    }

    @Test
    fun `Should return unchanged external trial map when trialMatches is empty`(){
        val externalTrial1 =
            ExternalTrial("Title of trial 1", setOf(Country.NETHERLANDS, Country.BELGIUM), "url", "NCT00000001")
        val externalTrial2 =
            ExternalTrial("Title of trial 2", setOf(Country.NETHERLANDS, Country.BELGIUM), "url", "NCT00000002")
        val externalTrialsPerEvent = mapOf(
            "event1" to listOf(externalTrial1, externalTrial2)
        )
        assertThat(filterAndGroupExternalTrialsByNctIdAndEvents(externalTrialsPerEvent, emptyList())).containsExactlyEntriesOf(
            externalTrialsPerEvent
        )
    }
}