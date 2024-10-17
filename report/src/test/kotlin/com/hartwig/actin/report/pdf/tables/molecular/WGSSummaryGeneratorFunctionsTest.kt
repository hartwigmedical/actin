package com.hartwig.actin.report.pdf.tables.molecular

import com.hartwig.actin.report.pdf.tables.clinical.CellTestUtil
import org.assertj.core.api.Assertions
import org.junit.Test

class WGSSummaryGeneratorFunctionsTest {

    @Test
    fun `Should return events concatenated and with a dubious quality string`() {
        val events = listOf("event 1", "event 2")
        val cell = WGSSummaryGeneratorFunctions.potentiallyActionableEventsCell(events)
        Assertions.assertThat(CellTestUtil.extractTextFromCell(cell)).isEqualTo("event 1 (dubious quality), event 2 (dubious quality)")
    }

    @Test
    fun `Should return none when list of events is empty`() {
        val events = emptyList<String>()
        val cell = WGSSummaryGeneratorFunctions.potentiallyActionableEventsCell(events)
        Assertions.assertThat(CellTestUtil.extractTextFromCell(cell)).isEqualTo("None")
    }
}