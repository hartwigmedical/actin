package com.hartwig.actin.report.pdf

import com.hartwig.actin.configuration.ReportConfiguration

object ReportWriterFactory {
    fun createProductionReportWriter(outputDirectory: String, reportConfiguration: ReportConfiguration): ReportWriter {
        return ReportWriter(true, outputDirectory, reportConfiguration)
    }

    fun createInMemoryReportWriter(): ReportWriter {
        return ReportWriter(false, null, ReportConfiguration())
    }
}