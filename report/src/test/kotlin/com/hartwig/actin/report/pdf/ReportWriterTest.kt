package com.hartwig.actin.report.pdf

import com.hartwig.actin.report.datamodel.TestReportFactory
import com.hartwig.actin.report.pdf.ReportWriterFactory.createInMemoryReportWriter
import org.junit.Test
import java.io.IOException

class ReportWriterTest {

    @Test
    @Throws(IOException::class)
    fun canGenerateInMemoryReports() {
        val memoryWriter = createInMemoryReportWriter()
        memoryWriter.write(TestReportFactory.createMinimalTestReport())
        memoryWriter.write(TestReportFactory.createProperTestReport())
        memoryWriter.write(TestReportFactory.createExhaustiveTestReport())
    }
}