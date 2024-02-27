package com.hartwig.actin.report.pdf

object ReportWriterFactory {
    fun createProductionTrialReportWriter(outputDirectory: String): ReportWriterTrial {
        return ReportWriterTrial(true, outputDirectory)
    }

    fun createInMemoryTrialReportWriter(): ReportWriterTrial {
        return ReportWriterTrial(false, null)
    }

    fun createProductionCRCReportWriter(outputDirectory: String): ReportWriterCRC {
        return ReportWriterCRC(true, outputDirectory)
    }

    fun createInMemoryCRCReportWriter(): ReportWriterCRC {
        return ReportWriterCRC(false, null)
    }
}