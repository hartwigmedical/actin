package com.hartwig.actin.report.pdf

object ReportWriterFactory {

    fun createProductionReportWriter(outputDirectory: String, labels: ReportLabels = ReportLabels.load()): ReportWriter {
        return ReportWriter(true, outputDirectory, labels)
    }

    fun createInMemoryReportWriter(labels: ReportLabels = ReportLabels.load()): ReportWriter {
        return ReportWriter(false, null, labels)
    }
}