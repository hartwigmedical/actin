package com.hartwig.actin.report.pdf.tables.molecular

import com.hartwig.actin.datamodel.molecular.Driver
import com.hartwig.actin.datamodel.molecular.DriverLikelihood
import com.hartwig.actin.datamodel.molecular.driver.TestCopyNumberFactory
import com.hartwig.actin.datamodel.molecular.driver.TestFusionFactory
import com.hartwig.actin.report.pdf.tables.clinical.CellTestUtil
import org.assertj.core.api.Assertions
import org.junit.Test

class WGSSummaryGeneratorFunctionsTest {

    @Test
    fun `Should return events concatenated and with warning string`() {
        val drivers = listOf(
            TestCopyNumberFactory.createMinimal().copy(event = "event 1", driverLikelihood = null),
            TestFusionFactory.createMinimal().copy(event = "event 2", driverLikelihood = null),
            TestFusionFactory.createMinimal().copy(event = "event 3", driverLikelihood = DriverLikelihood.MEDIUM)
        )
        val cell = WGSSummaryGeneratorFunctions.potentiallyActionableEventsCell(drivers)
        Assertions.assertThat(CellTestUtil.extractTextFromCell(cell))
            .isEqualTo("event 1, event 2 (dubious quality), event 3 (medium driver likelihood)")
    }

    @Test
    fun `Should return none when list of events is empty`() {
        val drivers = emptyList<Driver>()
        val cell = WGSSummaryGeneratorFunctions.potentiallyActionableEventsCell(drivers)
        Assertions.assertThat(CellTestUtil.extractTextFromCell(cell)).isEqualTo("None")
    }
}