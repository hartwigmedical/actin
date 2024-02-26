package com.hartwig.actin.report.pdf

import com.hartwig.actin.report.datamodel.TestReportFactory
import org.junit.Test
import java.io.IOException

class ReportWriterCRCTest {

    @Test
    @Throws(IOException::class)
    fun canGenerateInMemoryReports() {
        val memoryWriter = ReportWriterCRCFactory.createInMemoryReportWriter()
        memoryWriter.write(TestReportFactory.createMinimalTestReport())
        memoryWriter.write(TestReportFactory.createProperTestReport())
        memoryWriter.write(TestReportFactory.createExhaustiveTestReport())
    }
}