package com.hartwig.actin.report.pdf.tables.molecular

import com.hartwig.actin.datamodel.molecular.evidence.TestExternalTrialFactory
import com.hartwig.actin.report.trial.EventWithExternalTrial
import org.assertj.core.api.Assertions.assertThat
import org.assertj.core.api.Assertions.entry
import org.junit.Test

private val BASE_EXTERNAL_TRIAL_SUMMARY = TestExternalTrialFactory.create(
    nctId = "nct",
    title = "title",
    url = "url",
)

class DriverTableFunctionsTest {

    private val trial1 = BASE_EXTERNAL_TRIAL_SUMMARY.copy(nctId = "trial1")
    private val trial2 = BASE_EXTERNAL_TRIAL_SUMMARY.copy(nctId = "trial2")
    private val trial3 = BASE_EXTERNAL_TRIAL_SUMMARY.copy(nctId = "trial3")

    @Test
    fun `Should correctly group by single event`() {
        val externalTrials = setOf(
            EventWithExternalTrial("PTEN del", trial1),
            EventWithExternalTrial("PTEN del", trial2),
            EventWithExternalTrial("MYC amp", trial2),
            EventWithExternalTrial("PTEN del", trial3),
            EventWithExternalTrial("MYC amp", trial3)
        )
        val groupedByEvent = DriverTableFunctions.groupByEvent(externalTrials)
        assertThat(groupedByEvent).containsOnly(
            entry("PTEN del", "trial1, trial2, trial3"),
            entry("MYC amp", "trial2, trial3")
        )
    }
}