package com.hartwig.actin.system.example

import com.hartwig.actin.configuration.AlgoConfiguration
import com.hartwig.actin.configuration.EnvironmentConfiguration
import com.hartwig.actin.configuration.MolecularSummaryType
import com.hartwig.actin.configuration.ReportConfiguration
import com.hartwig.actin.testutil.ResourceLocator
import java.io.File
import java.time.LocalDate

const val LUNG_01_EXAMPLE = "LUNG-01"

private const val EXAMPLE_NAME_ = "<example_name>"

object ExampleFunctions {

    private const val REQUESTING_HOSPITAL = "Example"

    private const val EXAMPLE_TREATMENT_MATCH_DIRECTORY = "example_treatment_match"
    private const val EXAMPLE_TRIAL_DATABASE_DIRECTORY = "example_trial_database"
    private const val EXAMPLE_REPORT_DIRECTORY = "example_reports"

    private const val EXAMPLE_PATIENT_RECORD_JSON = "example_patient_data/EXAMPLE-$EXAMPLE_NAME_.patient_record.json"
    private const val EXAMPLE_TREATMENT_MATCH_JSON = "$EXAMPLE_TREATMENT_MATCH_DIRECTORY/EXAMPLE-$EXAMPLE_NAME_.treatment_match.json"
    private const val EXAMPLE_REPORT_PDF = "$EXAMPLE_REPORT_DIRECTORY/EXAMPLE-$EXAMPLE_NAME_.actin.pdf"
    private const val EXAMPLE_REPORT_EXTENDED_PDF = "$EXAMPLE_REPORT_DIRECTORY/EXAMPLE-$EXAMPLE_NAME_.actin.extended.pdf"

    fun resolveExamplePatientRecordJson(exampleName: String): String {
        return ResourceLocator.resourceOnClasspath(EXAMPLE_PATIENT_RECORD_JSON.replace(EXAMPLE_NAME_, exampleName))
    }

    fun resolveExampleTreatmentMatchJson(exampleName: String): String {
        return ResourceLocator.resourceOnClasspath(EXAMPLE_TREATMENT_MATCH_JSON.replace(EXAMPLE_NAME_, exampleName))
    }

    fun resolveExampleReportPdf(exampleName: String): String {
        return ResourceLocator.resourceOnClasspath(EXAMPLE_REPORT_PDF.replace(EXAMPLE_NAME_, exampleName))
    }

    fun resolveExampleReportExtendedPdf(exampleName: String): String {
        return ResourceLocator.resourceOnClasspath(EXAMPLE_REPORT_EXTENDED_PDF.replace(EXAMPLE_NAME_, exampleName))
    }

    fun resolveExampleTrialDatabaseDirectory(): String {
        return ResourceLocator.resourceOnClasspath(EXAMPLE_TRIAL_DATABASE_DIRECTORY)
    }

    fun resolveExampleTreatmentMatchOutputDirectory(): String {
        return listOf(systemTestResourcesDirectory(), EXAMPLE_TREATMENT_MATCH_DIRECTORY).joinToString(File.separator)
    }

    fun createExampleEnvironmentConfiguration(reportDate: LocalDate? = null): EnvironmentConfiguration {
        val base = EnvironmentConfiguration.create(null)
        return base.copy(
            requestingHospital = REQUESTING_HOSPITAL,
            algo = AlgoConfiguration(),
            report = ReportConfiguration(
                includeApprovedTreatmentsInSummary = false,
                includeMolecularDetailsChapter = false,
                includeClinicalDetailsChapter = false,
                reportDate = reportDate
            )
        )
    }

    fun createExhaustiveEnvironmentConfiguration(reportDate: LocalDate? = null): EnvironmentConfiguration {
        val base = EnvironmentConfiguration.create(null)
        return base.copy(
            requestingHospital = REQUESTING_HOSPITAL,
            algo = AlgoConfiguration(),
            report = ReportConfiguration(
                includeOverviewWithClinicalHistorySummary = true,
                includeMolecularDetailsChapter = true,
                includeIneligibleTrialsInSummary = true,
                includeSOCLiteratureEfficacyEvidence = true,
                includeEligibleSOCTreatmentSummary = true,
                molecularSummaryType = MolecularSummaryType.STANDARD,
                includeOtherOncologicalHistoryInSummary = true,
                includePatientHeader = true,
                includeRelevantNonOncologicalHistoryInSummary = true,
                includeApprovedTreatmentsInSummary = true,
                includeTrialMatchingInSummary = true,
                includeExternalTrialsInSummary = true,
                filterOnSOCExhaustionAndTumorType = true,
                includeClinicalDetailsChapter = true,
                includeTrialMatchingChapter = true,
                includeOnlyExternalTrialsInTrialMatching = true,
                includeLongitudinalMolecularChapter = true,
                includeMolecularEvidenceChapter = true,
                includeRawPathologyReport = true,
                includeTreatmentEvidenceRanking = true,
                reportDate = reportDate
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