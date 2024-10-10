package com.hartwig.actin.system

import com.hartwig.actin.PatientRecordJson
import com.hartwig.actin.algo.TreatmentMatcher
import com.hartwig.actin.algo.calendar.ReferenceDateProviderFactory
import com.hartwig.actin.algo.serialization.TreatmentMatchJson
import com.hartwig.actin.algo.util.TreatmentMatchPrinter
import com.hartwig.actin.testutil.ResourceLocator
import com.hartwig.actin.trial.serialization.TrialJson
import org.apache.commons.cli.ParseException
import org.apache.logging.log4j.LogManager
import org.apache.logging.log4j.Logger
import java.io.File
import kotlin.system.exitProcess

class LocalExampleTreatmentMatchApplication {

    private val examplePatientRecordJson = ResourceLocator.resourceOnClasspath("example_patient_data/EXAMPLE-LUNG-01.patient_record.json")
    private val exampleTrialDatabaseDir = ResourceLocator.resourceOnClasspath("example_trial_database")

    private val outputDirectory =
        listOf(LocalExampleFunctions.systemTestResourcesDirectory(), "example_treatment_match").joinToString(File.separator)

    fun run() {
        LOGGER.info("Loading patient record from {}", examplePatientRecordJson)
        val patient = PatientRecordJson.read(examplePatientRecordJson)

        LOGGER.info("Loading trial data from {}", exampleTrialDatabaseDir)
        val trials = TrialJson.readFromDir(exampleTrialDatabaseDir)

        val referenceDateProvider = ReferenceDateProviderFactory.create(patient, runHistorically = false)
        val resources = LocalExampleFunctions.createExampleRuleMappingResources(referenceDateProvider)

        LOGGER.info("Matching patient ${patient.patientId} to available trials")

        val match = TreatmentMatcher
            .create(
                resources = resources,
                trials = trials,
                efficacyEvidence = emptyList(),
                resistanceEvidenceMatcher = LocalExampleFunctions.createEmptyResistanceEvidenceMatcher()
            )
            .evaluateAndAnnotateMatchesForPatient(patient)

        TreatmentMatchPrinter.printMatch(match)
        TreatmentMatchJson.write(match, outputDirectory)

        LOGGER.info("Done!")
    }

    companion object {
        val LOGGER: Logger = LogManager.getLogger(LocalExampleTreatmentMatchApplication::class.java)
    }
}

fun main() {
    LocalExampleTreatmentMatchApplication.LOGGER.info("Running ACTIN Example Treatment Matcher")
    try {
        LocalExampleTreatmentMatchApplication().run()
    } catch (exception: ParseException) {
        LocalExampleTreatmentMatchApplication.LOGGER.warn(exception)
        exitProcess(1)
    }
}
