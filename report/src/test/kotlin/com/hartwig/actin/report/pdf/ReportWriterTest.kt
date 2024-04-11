package com.hartwig.actin.report.pdf

import com.hartwig.actin.report.datamodel.TestReportFactory
import org.junit.Test
import java.io.IOException

class ReportWriterTest {

    @Test
    @Throws(IOException::class)
    fun canGenerateInMemoryTrialReports() {
        val memoryWriter = ReportWriterFactory.createInMemoryReportWriter()
        memoryWriter.write(TestReportFactory.createMinimalTestReport())
        memoryWriter.write(TestReportFactory.createProperTestReport())
        memoryWriter.write(TestReportFactory.createExhaustiveTestReport())
    }
}