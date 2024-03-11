package com.hartwig.actin.report.interpretation

import com.hartwig.actin.molecular.datamodel.evidence.Country
import com.hartwig.actin.molecular.datamodel.evidence.ExternalTrial
import org.assertj.core.api.Assertions
import org.junit.Test

class AggregatedEvidenceInterpreterTest {

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
        Assertions.assertThat(AggregatedEvidenceInterpreter().groupExternalTrialsByNctIdAndEvents(evidence)).containsAllEntriesOf(mapOf(
            "event1,\nevent3" to listOf(externalTrialTargetingTwoEvents),
            "event2" to listOf(externalTrialTargetingOneEvent),
            )
        )
    }
}