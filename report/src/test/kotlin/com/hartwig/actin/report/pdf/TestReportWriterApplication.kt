package com.hartwig.actin.report.pdf

import com.hartwig.actin.PatientPrinter
import com.hartwig.actin.algo.serialization.TreatmentMatchJson
import com.hartwig.actin.algo.util.TreatmentMatchPrinter
import com.hartwig.actin.report.datamodel.Report
import com.hartwig.actin.report.datamodel.TestReportFactory
import com.hartwig.actin.report.pdf.ReportWriterFactory.createProductionReportWriter
import org.apache.logging.log4j.LogManager
import java.io.File

private val WORK_DIRECTORY = System.getProperty("user.dir")

object TestReportWriterApplication {

    private val LOGGER = LogManager.getLogger(TestReportWriterApplication::class.java)
    private val OPTIONAL_TREATMENT_MATCH_JSON = WORK_DIRECTORY + File.separator + "patient.treatment_match.json"

    fun createTestReport(skipMolecular: Boolean): Report {
        val report = if (skipMolecular) TestReportFactory.createExhaustiveTestReportWithoutMolecular() else
            TestReportFactory.createExhaustiveTestReport()
        LOGGER.info("Printing patient record")
        PatientPrinter.printRecord(report.patientRecord)

        val updated = if (File(OPTIONAL_TREATMENT_MATCH_JSON).exists()) {
            LOGGER.info("Loading treatment matches from {}", OPTIONAL_TREATMENT_MATCH_JSON)
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

fun main(args: Array<String>) {
    val skipMolecular = args.contains("--no-molecular")
    val writer = createProductionReportWriter(WORK_DIRECTORY)
    val report = TestReportWriterApplication.createTestReport(skipMolecular)
    writer.write(report)
}
