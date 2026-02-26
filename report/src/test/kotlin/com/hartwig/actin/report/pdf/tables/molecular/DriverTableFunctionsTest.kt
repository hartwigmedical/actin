package com.hartwig.actin.report.pdf.tables.molecular

import com.hartwig.actin.datamodel.molecular.driver.TestVariantFactory
import com.hartwig.actin.datamodel.molecular.evidence.Actionable
import com.hartwig.actin.datamodel.molecular.evidence.ExternalTrial
import com.hartwig.actin.datamodel.molecular.evidence.TestExternalTrialFactory
import com.hartwig.actin.report.trial.ActionableWithExternalTrial
import org.assertj.core.api.Assertions.assertThat
import org.assertj.core.api.Assertions.entry
import org.junit.jupiter.api.Test

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
            createEventWithExternalTrial("PTEN del", trial1),
            createEventWithExternalTrial("PTEN del", trial2),
            createEventWithExternalTrial("MYC amp", trial2),
            createEventWithExternalTrial("PTEN del", trial3),
            createEventWithExternalTrial("MYC amp", trial3)
        )
        val groupedByEvent = DriverTableFunctions.groupByEvent(externalTrials)
        assertThat(groupedByEvent).containsOnly(
            entry("PTEN del", "trial1, trial2, trial3"),
            entry("MYC amp", "trial2, trial3")
        )
    }

    private fun createEventWithExternalTrial(event: String, trial: ExternalTrial): ActionableWithExternalTrial {
        return ActionableWithExternalTrial(actionable = createActionable(event), trial = trial)
    }

    private fun createActionable(event: String): Actionable {
        return TestVariantFactory.createMinimal().copy(event = event)
    }
}