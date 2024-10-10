package com.hartwig.actin.system

import com.hartwig.actin.PatientRecordJson
import com.hartwig.actin.algo.serialization.TreatmentMatchJson
import com.hartwig.actin.report.datamodel.ReportFactory
import com.hartwig.actin.report.pdf.ReportWriterFactory
import org.apache.commons.cli.ParseException
import org.apache.logging.log4j.LogManager
import org.apache.logging.log4j.Logger
import kotlin.system.exitProcess

class LocalExampleReportApplication {

    fun run(examplePatientRecordJson: String, exampleTreatmentMatchJson: String, outputDirectory: String) {
        LOGGER.info("Loading patient record from {}", examplePatientRecordJson)
        val patient = PatientRecordJson.read(examplePatientRecordJson)

        LOGGER.info("Loading treatment match results from {}", exampleTreatmentMatchJson)
        val treatmentMatch = TreatmentMatchJson.read(exampleTreatmentMatchJson)

        val environmentConfig = ExampleFunctions.createExampleEnvironmentConfiguration()
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
        val examplePatientRecordJson = ExampleFunctions.resolveExamplePatientRecordJson()
        val exampleTreatmentMatchJson = ExampleFunctions.resolveExampleTreatmentMatchJson()
        val outputDirectory = ExampleFunctions.resolveExampleReportOutputDirectory()

        LocalExampleReportApplication().run(examplePatientRecordJson, exampleTreatmentMatchJson, outputDirectory)
    } catch (exception: ParseException) {
        LocalExampleReportApplication.LOGGER.warn(exception)
        exitProcess(1)
    }
}
