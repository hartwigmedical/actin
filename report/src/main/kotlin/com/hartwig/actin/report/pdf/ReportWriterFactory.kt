package com.hartwig.actin.report.pdf

object ReportWriterFactory {
    fun createProductionReportWriter(outputDirectory: String): ReportWriterTrial {
        return ReportWriterTrial(true, outputDirectory)
    }

    fun createInMemoryReportWriter(): ReportWriterTrial {
        return ReportWriterTrial(false, null)
    }
}