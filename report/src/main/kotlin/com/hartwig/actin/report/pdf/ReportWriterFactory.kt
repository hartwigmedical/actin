package com.hartwig.actin.report.pdf

object ReportWriterFactory {
    fun createProductionReportWriter(outputDirectory: String): ReportWriter {
        return ReportWriter(true, outputDirectory)
    }

    fun createInMemoryReportWriter(): ReportWriter {
        return ReportWriter(false, null)
    }
}