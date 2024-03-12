package com.hartwig.actin.report.interpretation

import com.hartwig.actin.algo.datamodel.TrialMatch
import com.hartwig.actin.molecular.datamodel.evidence.Country
import com.hartwig.actin.molecular.datamodel.evidence.ExternalTrial
import org.assertj.core.api.Assertions.assertThat
import org.junit.Test
import com.hartwig.actin.report.interpretation.AggregatedEvidenceInterpreter.groupExternalTrialsByNctIdAndEvents
import com.hartwig.actin.report.interpretation.AggregatedEvidenceInterpreter.filterExternalTrialsBasedOnNctId
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
    private val externalTrialWithMatchToLocal =
        ExternalTrial("Title of trial 1", setOf(Country.NETHERLANDS, Country.BELGIUM), "url", "NCT00000001")
    private val externalTrialWithoutMatchToLocal =
        ExternalTrial("Title of trial 2", setOf(Country.NETHERLANDS, Country.BELGIUM), "url", "NCT00000002")
    private val externalTrialsPerEvent = mapOf(
        "event1" to listOf(externalTrialWithMatchToLocal, externalTrialWithoutMatchToLocal),
        "event2" to listOf(externalTrialWithMatchToLocal),
        "event3" to listOf(externalTrialWithoutMatchToLocal)
    )

    private val externalTrialTargetingTwoEvents =
        ExternalTrial("Trial targeting event 1 and event 3", setOf(Country.NETHERLANDS, Country.BELGIUM), "url", "NCT00000010")
    private val externalTrialTargetingOneEvent =
        ExternalTrial("Trial targeting event 2", setOf(Country.NETHERLANDS, Country.BELGIUM), "url", "NCT00000011")
    private val evidence = mapOf(
        "event1" to listOf(externalTrialTargetingTwoEvents),
        "event2" to listOf(externalTrialTargetingOneEvent),
        "event3" to listOf(externalTrialTargetingTwoEvents)
    )

    @Test
    fun `Should correctly group trials with identical nctIds combining all events of these trials`(){
        assertThat(groupExternalTrialsByNctIdAndEvents(evidence)).containsAllEntriesOf(mapOf(
            "event1,\nevent3" to listOf(externalTrialTargetingTwoEvents),
            "event2" to listOf(externalTrialTargetingOneEvent),
            )
        )
    }

    @Test
    fun `Should not filter out external trials without nctId match among local trials and should maintain event to trial mapping`(){
        assertThat(filterExternalTrialsBasedOnNctId(externalTrialsPerEvent, trialMatches)).containsExactlyEntriesOf(
            mapOf(
                "event1" to listOf(externalTrialWithoutMatchToLocal),
                "event3" to listOf(externalTrialWithoutMatchToLocal)
            )
        )
    }

    @Test
    fun `Should return unchanged external trial map when trialMatches is empty`(){
        assertThat(filterExternalTrialsBasedOnNctId(externalTrialsPerEvent, emptyList())).containsExactlyEntriesOf(externalTrialsPerEvent)
    }
}