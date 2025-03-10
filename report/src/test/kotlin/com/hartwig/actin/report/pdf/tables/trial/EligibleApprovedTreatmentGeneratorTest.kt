package com.hartwig.actin.report.pdf.tables.trial

import com.hartwig.actin.report.datamodel.TestReportFactory
import org.assertj.core.api.Assertions.assertThat
import org.junit.Test

private val width: Float = 1F

class EligibleApprovedTreatmentGeneratorTest {
    @Test
    fun `Should return approved treatments if available`() {
        val report = TestReportFactory.createMinimalTestReport()
        val contents = EligibleApprovedTreatmentGenerator(report, width).contents()
        assertThat(contents != null)
    }
}

