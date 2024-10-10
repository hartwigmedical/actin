package com.hartwig.actin.system

import com.hartwig.actin.configuration.AlgoConfiguration
import com.hartwig.actin.configuration.EnvironmentConfiguration
import com.hartwig.actin.configuration.ReportConfiguration
import com.hartwig.actin.testutil.ResourceLocator
import java.io.File

object ExampleFunctions {

    private const val TRIAL_SOURCE = "Example"

    private const val EXAMPLE_PATIENT_RECORD_JSON = "example_patient_data/EXAMPLE-LUNG-01.patient_record.json"
    private const val EXAMPLE_TREATMENT_MATCH_JSON = "example_treatment_match/EXAMPLE-LUNG-01.treatment_match.json"
    private const val EXAMPLE_TRIAL_DATABASE_DIRECTORY = "example_trial_database"

    private const val EXAMPLE_TREATMENT_MATCH_DIRECTORY = "example_treatment_match"
    private const val EXAMPLE_REPORT_DIRECTORY = "example_reports"

    fun resolveExamplePatientRecordJson(): String {
        return ResourceLocator.resourceOnClasspath(EXAMPLE_PATIENT_RECORD_JSON)
    }

    fun resolveExampleTreatmentMatchJson(): String {
        return ResourceLocator.resourceOnClasspath(EXAMPLE_TREATMENT_MATCH_JSON)
    }

    fun resolveExampleTrialDatabaseDirectory(): String {
        return ResourceLocator.resourceOnClasspath(EXAMPLE_TRIAL_DATABASE_DIRECTORY)
    }

    fun resolveExampleTreatmentMatchOutputDirectory(): String {
        return listOf(systemTestResourcesDirectory(), EXAMPLE_TREATMENT_MATCH_DIRECTORY).joinToString(File.separator)
    }

    fun resolveExampleReportOutputDirectory(): String {
        return listOf(systemTestResourcesDirectory(), EXAMPLE_REPORT_DIRECTORY).joinToString(File.separator)
    }

    fun createExampleEnvironmentConfiguration(): EnvironmentConfiguration {
        val base = EnvironmentConfiguration.create(null)
        return base.copy(
            algo = AlgoConfiguration(trialSource = TRIAL_SOURCE),
            report = ReportConfiguration(
                includeApprovedTreatmentsInSummary = false,
                includeExternalTrialsInSummary = false,
                includeMolecularDetailsChapter = false,
                includeClinicalDetailsChapter = false
            )
        )
    }

    private fun systemTestResourcesDirectory(): String {
        return listOf(
            System.getProperty("user.home"),
            "hmf",
            "repos",
            "actin",
            "system",
            "src",
            "test",
            "resources"
        ).joinToString(File.separator)
    }
}