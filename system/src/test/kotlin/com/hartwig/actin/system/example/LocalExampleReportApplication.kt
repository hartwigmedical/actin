package com.hartwig.actin.system.example

import com.hartwig.actin.PatientRecordJson
import com.hartwig.actin.algo.serialization.TreatmentMatchJson
import com.hartwig.actin.report.datamodel.ReportFactory
import com.hartwig.actin.report.pdf.ReportWriterFactory
import java.time.LocalDate
import kotlin.system.exitProcess
import org.apache.commons.cli.ParseException
import org.apache.logging.log4j.LogManager
import org.apache.logging.log4j.Logger

class LocalExampleReportApplication(private val reportingDate: LocalDate) {

    fun run(examplePatientRecordJson: String, exampleTreatmentMatchJson: String, outputDirectory: String) {
        LOGGER.info("Loading patient record from {}", examplePatientRecordJson)
        val patient = PatientRecordJson.read(examplePatientRecordJson)

        LOGGER.info("Loading treatment match results from {}", exampleTreatmentMatchJson)
        val treatmentMatch = TreatmentMatchJson.read(exampleTreatmentMatchJson)

        val environmentConfig = ExampleFunctions.createExampleEnvironmentConfiguration(reportingDate)
        val report = ReportFactory.fromInputs(patient, treatmentMatch, environmentConfig.report)
        val writer = ReportWriterFactory.createProductionReportWriter(outputDirectory)

        writer.write(report, enableExtendedMode = false)
        writer.write(report, enableExtendedMode = true)

        LOGGER.info("Done!")
    }

    companion object {
        val LOGGER: Logger = LogManager.getLogger(LocalExampleReportApplication::class.java)
    }
}

fun main() {
    LocalExampleReportApplication.LOGGER.info("Running ACTIN Example Reporter")
    try {
        val examplePatientRecordJson = ExampleFunctions.resolveExamplePatientRecordJson(LUNG_01_EXAMPLE)
        val exampleTreatmentMatchJson = ExampleFunctions.resolveExampleTreatmentMatchJson(LUNG_01_EXAMPLE)
        val outputDirectory = ExampleFunctions.resolveExampleReportOutputDirectory()

        LocalExampleReportApplication(LocalDate.now()).run(examplePatientRecordJson, exampleTreatmentMatchJson, outputDirectory)
    } catch (exception: ParseException) {
        LocalExampleReportApplication.LOGGER.warn(exception)
        exitProcess(1)
    }
}
