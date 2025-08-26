package com.hartwig.actin.system.example

import com.hartwig.actin.PatientRecordJson
import com.hartwig.actin.algo.serialization.TreatmentMatchJson
import com.hartwig.actin.configuration.EnvironmentConfiguration
import com.hartwig.actin.report.datamodel.ReportFactory
import com.hartwig.actin.report.pdf.ReportWriterFactory
import org.apache.commons.cli.ParseException
import org.apache.logging.log4j.LogManager
import org.apache.logging.log4j.Logger
import java.time.LocalDate
import java.util.Locale
import kotlin.system.exitProcess

private const val TRIAL_EXAMPLE_TO_RUN = LUNG_01_EXAMPLE
private const val PERSONALIZATION_EXAMPLE_TO_RUN = CRC_01_EXAMPLE

class LocalExampleReportApplication {

    fun run(
        reportDate: LocalDate,
        examplePatientRecordJson: String,
        exampleTreatmentMatchJson: String,
        outputDirectory: String,
        environmentConfiguration: EnvironmentConfiguration
    ) {
        LOGGER.info("Loading patient record from {}", examplePatientRecordJson)
        val patient = PatientRecordJson.read(examplePatientRecordJson)

        LOGGER.info("Loading treatment match results from {}", exampleTreatmentMatchJson)
        val treatmentMatch = TreatmentMatchJson.read(exampleTreatmentMatchJson)

        val report = ReportFactory.create(reportDate, patient, treatmentMatch, environmentConfiguration)
        val writer = ReportWriterFactory.createProductionReportWriter(outputDirectory)

        writer.write(report, enableExtendedMode = false)
        writer.write(report, enableExtendedMode = true)

        LOGGER.info("Done!")
    }

    fun runExample(
        exampleToRun: String,
        environmentConfigProvider: () -> EnvironmentConfiguration
    ) {
        val localOutputPath = System.getProperty("user.home") + "/hmf/tmp"

        try {
            val examplePatientRecordJson = ExampleFunctions.resolveExamplePatientRecordJson(exampleToRun)
            val exampleTreatmentMatchJson = ExampleFunctions.resolveExampleTreatmentMatchJson(exampleToRun)
            val localExampleReportApplication = LocalExampleReportApplication()
            localExampleReportApplication.run(
                LocalDate.now(),
                examplePatientRecordJson,
                exampleTreatmentMatchJson,
                localOutputPath,
                environmentConfigProvider()
            )
        } catch (exception: ParseException) {
            LOGGER.warn(exception)
            exitProcess(1)
        }
    }

    companion object {
        val LOGGER: Logger = LogManager.getLogger(LocalExampleReportApplication::class.java)
    }
}

fun main() {
    Locale.setDefault(Locale.US)

    LocalExampleReportApplication.LOGGER.info("Running ACTIN Trial Example Reporter")
    LocalExampleReportApplication().runExample(TRIAL_EXAMPLE_TO_RUN) { ExampleFunctions.createTrialMatchingEnvironmentConfiguration() }

    LocalExampleReportApplication.LOGGER.info("Running ACTIN Personalization Example Reporter")
    LocalExampleReportApplication().runExample(PERSONALIZATION_EXAMPLE_TO_RUN) {
        ExampleFunctions.createPersonalizationEnvironmentConfiguration()
    }
}
