package com.hartwig.actin.report.pdf

import com.hartwig.actin.configuration.EnvironmentConfiguration
import com.hartwig.actin.report.datamodel.TestReportFactory
import org.junit.Test
import java.io.IOException

class ReportWriterTest {
    private val reports = listOf(
        TestReportFactory.createMinimalTestReport(),
        TestReportFactory.createProperTestReport(),
        TestReportFactory.createExhaustiveTestReport()
    )
    private val memoryWriter = ReportWriterFactory.createInMemoryReportWriter()

    @Test
    @Throws(IOException::class)
    fun canGenerateInMemoryTrialReports() {
        reports.forEach(memoryWriter::write)
    }

    @Test
    @Throws(IOException::class)
    fun canGenerateInMemoryCRCReports() {
        val crcConfig = EnvironmentConfiguration.create(null, "CRC").report
        reports.forEach { memoryWriter.write(it.copy(config = crcConfig)) }
    }
}