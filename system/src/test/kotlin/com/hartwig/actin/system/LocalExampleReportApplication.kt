package com.hartwig.actin.system

import com.hartwig.actin.PatientRecordJson
import com.hartwig.actin.algo.serialization.TreatmentMatchJson
import com.hartwig.actin.report.datamodel.ReportFactory
import com.hartwig.actin.report.pdf.ReportWriterFactory
import com.hartwig.actin.testutil.ResourceLocator
import org.apache.commons.cli.ParseException
import org.apache.logging.log4j.LogManager
import org.apache.logging.log4j.Logger
import java.io.File
import kotlin.system.exitProcess

class LocalExampleReportApplication {

    private val examplePatientRecordJson = ResourceLocator.resourceOnClasspath("example_patient_data/EXAMPLE-LUNG-01.patient_record.json")
    private val exampleTreatmentMatchJson =
        ResourceLocator.resourceOnClasspath("example_treatment_match/EXAMPLE-LUNG-01.treatment_match.json")

    private val outputDirectory =
        listOf(LocalExampleFunctions.systemTestResourcesDirectory(), "example_reports").joinToString(File.separator)

    fun run() {
        LOGGER.info("Loading patient record from {}", examplePatientRecordJson)
        val patient = PatientRecordJson.read(examplePatientRecordJson)

        LOGGER.info("Loading treatment match results from {}", exampleTreatmentMatchJson)
        val treatmentMatch = TreatmentMatchJson.read(exampleTreatmentMatchJson)

        val environmentConfig = LocalExampleFunctions.createExampleEnvironmentConfiguration()
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
        LocalExampleReportApplication().run()
    } catch (exception: ParseException) {
        LocalExampleReportApplication.LOGGER.warn(exception)
        exitProcess(1)
    }
}
