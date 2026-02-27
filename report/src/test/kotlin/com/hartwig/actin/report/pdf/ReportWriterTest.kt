package com.hartwig.actin.report.pdf

import com.hartwig.actin.configuration.ReportConfiguration
import com.hartwig.actin.doid.TestDoidModelFactory
import com.hartwig.actin.report.datamodel.TestReportFactory
import org.junit.Test

class ReportWriterTest {

    private val reports = listOf(
        TestReportFactory.createMinimalTestReport(),
        TestReportFactory.createProperTestReport(),
        TestReportFactory.createExhaustiveTestReport(),
        TestReportFactory.createExhaustiveTestReportWithOtherLocations()
    )
    private val memoryWriter = ReportWriterFactory.createInMemoryReportWriter()

    @Test
    fun `Should generate in-memory reports`() {
        reports.forEach {
            memoryWriter.write(
                it,
                configuration = ReportConfiguration.extended(),
                TestDoidModelFactory.createMinimalTestDoidModel(),
                addExtendedSuffix = true
            )
        }
    }
}