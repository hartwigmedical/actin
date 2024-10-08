package com.hartwig.actin.system

import com.hartwig.actin.PatientRecordJson
import com.hartwig.actin.algo.TreatmentMatcher
import com.hartwig.actin.algo.calendar.ReferenceDateProviderFactory
import com.hartwig.actin.algo.serialization.TreatmentMatchJson
import com.hartwig.actin.algo.util.TreatmentMatchPrinter
import com.hartwig.actin.trial.serialization.TrialJson
import org.apache.commons.cli.ParseException
import org.apache.logging.log4j.LogManager
import org.apache.logging.log4j.Logger
import java.io.File
import kotlin.system.exitProcess

class TestTreatmentMatchGenerationApplication {

    private val testPatientRecordJson = resourceOnClasspath("test_patient_data/EXAMPLE-LUNG-01.patient_record.json")
    private val testTrialRecordDatabaseDir = resourceOnClasspath("test_trial_database")

    private val outputDirectory = listOf(
        System.getProperty("user.home"),
        "hmf",
        "repos",
        "actin",
        "system",
        "src",
        "test",
        "resources",
        "test_treatment_match"
    ).joinToString(File.separator)

    fun run() {
        LOGGER.info("Loading patient record from {}", testPatientRecordJson)
        val patient = PatientRecordJson.read(testPatientRecordJson)

        LOGGER.info("Loading trial data from {}", testTrialRecordDatabaseDir)
        val trials = TrialJson.readFromDir(testTrialRecordDatabaseDir)

        val referenceDateProvider = ReferenceDateProviderFactory.create(patient, runHistorically = false)
        val resources = TestFunctions.createTestRuleMappingResources(referenceDateProvider)

        LOGGER.info("Matching patient to available trials")
        val match = TreatmentMatcher
            .create(
                resources = resources,
                trials = trials,
                efficacyEvidence = emptyList(),
                resistanceEvidenceMatcher = TestFunctions.createMockResistanceEvidenceMatcher()
            )
            .evaluateAndAnnotateMatchesForPatient(patient)

        TreatmentMatchPrinter.printMatch(match)
        TreatmentMatchJson.write(match, outputDirectory)
        LOGGER.info("Done!")
    }

    private fun resourceOnClasspath(relativePath: String): String {
        return Companion::class.java.getResource("/" + relativePath.removePrefix("/"))!!.path
    }

    companion object {
        val LOGGER: Logger = LogManager.getLogger(TestTreatmentMatchGenerationApplication::class.java)
        const val APPLICATION = "ACTIN Test Treatment Matcher"
    }
}

fun main() {
    TestTreatmentMatchGenerationApplication.LOGGER.info("Running {}", TestTreatmentMatchGenerationApplication.APPLICATION)
    try {
        TestTreatmentMatchGenerationApplication().run()
    } catch (exception: ParseException) {
        TestTreatmentMatchGenerationApplication.LOGGER.warn(exception)
        exitProcess(1)
    }
}
