package com.hartwig.actin.report.pdf.tables.molecular

import com.hartwig.actin.report.pdf.tables.trial.ExternalTrialSummary
import org.assertj.core.api.Assertions.assertThat
import org.assertj.core.api.Assertions.entry
import org.junit.Test

private val BASE_EXTERNAL_TRIAL_SUMMARY = ExternalTrialSummary(
    nctId = "nct",
    title = "title",
    url = "url",
    actinMolecularEvents = sortedSetOf(),
    sourceMolecularEvents = sortedSetOf(),
    applicableCancerTypes = sortedSetOf(),
    countries = sortedSetOf()
)

class DriverTableFunctionsTest {

    private val trial1 = BASE_EXTERNAL_TRIAL_SUMMARY.copy(title = "trial 1", actinMolecularEvents = sortedSetOf("PTEN del"))
    private val trial2 = BASE_EXTERNAL_TRIAL_SUMMARY.copy(title = "trial 2", actinMolecularEvents = sortedSetOf("PTEN del", "MYC amp"))
    private val trial3 = BASE_EXTERNAL_TRIAL_SUMMARY.copy(title = "trial 3", actinMolecularEvents = sortedSetOf("PTEN del", "MYC amp"))

    @Test
    fun `Should correctly group by single event`() {
        val externalTrials = setOf(trial1, trial2, trial3)
        val groupedByEvent = DriverTableFunctions.groupByEvent(externalTrials)
        assertThat(groupedByEvent).containsOnly(
            entry("MYC amp", setOf(trial2, trial3)),
            entry("PTEN del", setOf(trial1, trial2, trial3))
        )
    }
}