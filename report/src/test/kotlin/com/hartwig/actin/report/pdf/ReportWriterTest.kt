package com.hartwig.actin.report.pdf

import com.hartwig.actin.configuration.EnvironmentConfiguration
import com.hartwig.actin.report.datamodel.TestReportFactory
import org.junit.Test

class ReportWriterTest {

    private val reports = listOf(
        TestReportFactory.createMinimalTestReport(),
        TestReportFactory.createProperTestReport(),
        TestReportFactory.createExhaustiveTestReport(),
        TestReportFactory.createExhaustiveTestReportWithOtherLocations()
    )
    private val memoryWriter = ReportWriterFactory.createProductionReportWriter("/Users/andreiaesteves/hmf/repos/actin")

    @Test
    fun `Should generate in-memory trial matching reports`() {
        reports.forEach(memoryWriter::write)
    }

    @Test
    fun `Should generate in-memory CRC reports`() {
        val crcConfig = EnvironmentConfiguration.create(null, "CRC").report
        reports.forEach { memoryWriter.write(it.copy(config = crcConfig)) }
    }
}