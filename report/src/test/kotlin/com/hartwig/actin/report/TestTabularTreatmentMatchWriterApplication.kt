package com.hartwig.actin.report

import com.hartwig.actin.report.datamodel.TestReportFactory
import org.apache.logging.log4j.LogManager
import java.io.File
import java.io.IOException

object TestTabularTreatmentMatchWriterApplication {
    private val LOGGER = LogManager.getLogger(TestTabularTreatmentMatchWriterApplication::class.java)
    private val WORK_DIRECTORY = System.getProperty("user.home") + File.separator + "hmf" + File.separator + "tmp"

    @Throws(IOException::class)
    @JvmStatic
    fun main(args: Array<String>) {
        writeTestTabularTreatmentMatches()
    }

    @Throws(IOException::class)
    private fun writeTestTabularTreatmentMatches() {
        val treatmentMatch = TestReportFactory.createProperTestReport().treatmentMatch
        val filename = WORK_DIRECTORY + File.separator + treatmentMatch.patientId()
        val summaryFile = "$filename.evaluation.summary.tsv"
        LOGGER.info("Printing summary to $summaryFile")
        TabularTreatmentMatchWriter.writeEvaluationSummaryToTsv(treatmentMatch, summaryFile)
        val detailFile = "$filename.evaluation.details.tsv"
        LOGGER.info("Printing details to $detailFile")
        TabularTreatmentMatchWriter.writeEvaluationDetailsToTsv(treatmentMatch, detailFile)
        LOGGER.info("Done")
    }
}