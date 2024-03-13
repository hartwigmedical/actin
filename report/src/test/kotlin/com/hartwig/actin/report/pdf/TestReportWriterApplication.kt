package com.hartwig.actin.report.pdf

import com.hartwig.actin.algo.serialization.TreatmentMatchJson
import com.hartwig.actin.algo.util.TreatmentMatchPrinter
import com.hartwig.actin.clinical.util.ClinicalPrinter
import com.hartwig.actin.molecular.util.MolecularPrinter
import com.hartwig.actin.report.datamodel.Report
import com.hartwig.actin.report.datamodel.TestReportFactory
import com.hartwig.actin.report.pdf.ReportWriterFactory.createProductionReportWriter
import org.apache.logging.log4j.LogManager
import java.io.File

object TestReportWriterApplication {

    private val LOGGER = LogManager.getLogger(TestReportWriterApplication::class.java)

    private val WORK_DIRECTORY = System.getProperty("user.home") + File.separator + "hmf" + File.separator + "tmp"
    private val OPTIONAL_TREATMENT_MATCH_JSON = WORK_DIRECTORY + File.separator + "patient.treatment_match.json"

    @JvmStatic
    fun main(args: Array<String>) {
        val writer = createProductionReportWriter(WORK_DIRECTORY)
        val report = createTestReport()
        writer.write(report)
    }

    private fun createTestReport(): Report {
        val report = TestReportFactory.createExhaustiveTestReport()
        LOGGER.info("Printing clinical record")
        ClinicalPrinter.printRecord(report.clinical)
        LOGGER.info("Printing molecular record")
        // TODO (kz) this will blow up when no wgs in molecular history, fix!
        MolecularPrinter.printRecord(report.molecularHistory.mostRecentWGS()!!)

        val updated = if (File(OPTIONAL_TREATMENT_MATCH_JSON).exists()) {
            LOGGER.info(
                "Loading treatment matches from {}",
                OPTIONAL_TREATMENT_MATCH_JSON
            )
            val match = TreatmentMatchJson.read(OPTIONAL_TREATMENT_MATCH_JSON)
            report.copy(treatmentMatch = match)
        } else {
            report
        }

        LOGGER.info("Printing treatment match results")
        TreatmentMatchPrinter.printMatch(updated.treatmentMatch)
        return updated
    }
}