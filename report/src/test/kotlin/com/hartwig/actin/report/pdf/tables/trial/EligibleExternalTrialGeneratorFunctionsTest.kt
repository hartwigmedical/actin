package com.hartwig.actin.report.pdf.tables.trial

import com.hartwig.actin.molecular.datamodel.evidence.Country
import com.hartwig.actin.molecular.datamodel.evidence.ExternalTrial
import org.assertj.core.api.Assertions.assertThat
import org.junit.Test

class EligibleExternalTrialGeneratorFunctionsTest {
    private val externalTrial1 = ExternalTrial("title1", setOf(Country.NETHERLANDS, Country.GERMANY), "url1", "nctId1")
    private val externalTrial2 = ExternalTrial("title2", setOf(Country.BELGIUM), "url2", "nctId2")
    private val externalTrial3 = ExternalTrial("title3", setOf(Country.NETHERLANDS), "url3", "nctId3")
    private val externalTrial4 = ExternalTrial("title4", setOf(Country.GERMANY), "url4", "nctId4")

    private val externalTrialsByEvent = mapOf(
        "event1" to listOf(externalTrial1, externalTrial2),
        "event2" to listOf(externalTrial3),
        "event3" to listOf(externalTrial4)
    )

    @Test
    fun `Should return map of lists containing Dutch trials`() {
        assertThat(EligibleExternalTrialGeneratorFunctions.localTrials(externalTrialsByEvent))
            .isEqualTo(mapOf("event1" to listOf(externalTrial1), "event2" to listOf(externalTrial3)))
    }

    @Test
    fun `Should return map of lists containing non-Dutch trials`() {
        assertThat(EligibleExternalTrialGeneratorFunctions.nonLocalTrials(externalTrialsByEvent))
            .isEqualTo(mapOf("event1" to listOf(externalTrial2), "event3" to listOf(externalTrial4)))
    }
}