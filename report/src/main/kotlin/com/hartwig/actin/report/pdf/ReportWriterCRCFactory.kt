package com.hartwig.actin.report.pdf

object ReportWriterCRCFactory {
    fun createProductionReportWriter(outputDirectory: String): ReportWriterCRC {
        return ReportWriter(true, outputDirectory)
    }

    fun createInMemoryReportWriter(): ReportWriterCRC {
        return ReportWriterCRC(false, null)
    }
}