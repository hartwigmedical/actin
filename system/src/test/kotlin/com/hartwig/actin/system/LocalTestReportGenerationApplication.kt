package com.hartwig.actin.system

import com.hartwig.actin.PatientRecordJson
import com.hartwig.actin.algo.serialization.TreatmentMatchJson
import com.hartwig.actin.report.datamodel.ReportFactory
import com.hartwig.actin.report.pdf.ReportWriterFactory
import org.apache.commons.cli.ParseException
import org.apache.logging.log4j.LogManager
import org.apache.logging.log4j.Logger
import java.io.File
import kotlin.system.exitProcess

class LocalTestReportGenerationApplication {

    private val testPatientRecordJson = LocalTestFunctions.resourceOnClasspath("test_patient_data/EXAMPLE-LUNG-01.patient_record.json")
    private val testTreatmentMatchJson = LocalTestFunctions.resourceOnClasspath("test_treatment_match/EXAMPLE-LUNG-01.treatment_match.json")

    private val outputDirectory = listOf(System.getProperty("user.home"), "hmf", "tmp").joinToString(File.separator)

    fun run() {
        LOGGER.info("Loading patient record from {}", testPatientRecordJson)
        val patient = PatientRecordJson.read(testPatientRecordJson)

        LOGGER.info("Loading treatment match results from {}", testTreatmentMatchJson)
        val treatmentMatch = TreatmentMatchJson.read(testTreatmentMatchJson)

        val environmentConfig = LocalTestFunctions.createTestEnvironmentConfiguration()
        val report = ReportFactory.fromInputs(patient, treatmentMatch, environmentConfig.report)
        val writer = ReportWriterFactory.createProductionReportWriter(outputDirectory)
        writer.write(report, false)

        LOGGER.info("Done!")
    }

    companion object {
        val LOGGER: Logger = LogManager.getLogger(LocalTestReportGenerationApplication::class.java)
    }
}

fun main() {
    LocalTestReportGenerationApplication.LOGGER.info("Running ACTIN Test Reporter")
    try {
        LocalTestReportGenerationApplication().run()
    } catch (exception: ParseException) {
        LocalTestReportGenerationApplication.LOGGER.warn(exception)
        exitProcess(1)
    }
}
