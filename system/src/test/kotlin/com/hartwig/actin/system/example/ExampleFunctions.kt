package com.hartwig.actin.system.example

import com.hartwig.actin.PatientRecordJson
import com.hartwig.actin.algo.serialization.TreatmentMatchJson
import com.hartwig.actin.configuration.ClinicalSummaryType
import com.hartwig.actin.configuration.MolecularSummaryType
import com.hartwig.actin.configuration.ReportConfiguration
import com.hartwig.actin.datamodel.molecular.evidence.Country
import com.hartwig.actin.report.datamodel.ReportFactory
import com.hartwig.actin.report.pdf.ReportWriterFactory
import com.hartwig.actin.testutil.ResourceLocator
import org.apache.commons.cli.ParseException
import org.apache.logging.log4j.LogManager
import java.io.File
import java.time.LocalDate
import kotlin.system.exitProcess

const val LUNG_01_EXAMPLE = "LUNG-01"
const val LUNG_02_EXAMPLE = "LUNG-02"
const val LUNG_03_EXAMPLE = "LUNG-03"
const val LUNG_04_EXAMPLE = "LUNG-04"
const val CRC_01_EXAMPLE = "CRC-01"

private const val EXAMPLE_NAME_ = "<example_name>"

object ExampleFunctions {

    private val LOGGER = LogManager.getLogger(ExampleFunctions::class.java)

    private const val HOSPITAL_OF_REFERENCE = "Example"

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

    fun createTrialMatchingReportConfiguration(): ReportConfiguration {
        return ReportConfiguration().copy(
            clinicalSummaryType = ClinicalSummaryType.TRIAL_MATCHING_MINIMAL,
            includeApprovedTreatmentsInSummary = false,
            countryOfReference = Country.NETHERLANDS,
            hospitalOfReference = HOSPITAL_OF_REFERENCE
        )
    }

    fun createPersonalizationReportConfiguration(): ReportConfiguration {
        return ReportConfiguration().copy(
            clinicalSummaryType = ClinicalSummaryType.CRC_PERSONALIZATION,
            includeOverviewWithClinicalHistorySummary = true,
            includeMolecularDetailsChapter = false,
            includeApprovedTreatmentsInSummary = false,
            includeSOCLiteratureEfficacyEvidence = true,
            includeEligibleSOCTreatmentSummary = true,
            molecularSummaryType = MolecularSummaryType.NONE,
            filterOnSOCExhaustionAndTumorType = true,
            countryOfReference = Country.NETHERLANDS,
            hospitalOfReference = HOSPITAL_OF_REFERENCE
        )
    }
    
    fun runExample(exampleToRun: String, reportConfigProvider: () -> ReportConfiguration) {
        val localOutputPath = System.getProperty("user.home") + "/hmf/tmp"

        try {
            val examplePatientRecordJson = resolveExamplePatientRecordJson(exampleToRun)
            val exampleTreatmentMatchJson = resolveExampleTreatmentMatchJson(exampleToRun)
            run(LocalDate.now(), examplePatientRecordJson, exampleTreatmentMatchJson, localOutputPath, reportConfigProvider())
        } catch (exception: ParseException) {
            LOGGER.warn(exception)
            exitProcess(1)
        }
    }

    fun run(
        reportDate: LocalDate,
        examplePatientRecordJson: String,
        exampleTreatmentMatchJson: String,
        outputDirectory: String,
        reportConfiguration: ReportConfiguration
    ) {
        LOGGER.info("Loading patient record from {}", examplePatientRecordJson)
        val patient = PatientRecordJson.read(examplePatientRecordJson)

        LOGGER.info("Loading treatment match results from {}", exampleTreatmentMatchJson)
        val treatmentMatch = TreatmentMatchJson.read(exampleTreatmentMatchJson)

        val report = ReportFactory.create(reportDate, patient, treatmentMatch, reportConfiguration)
        val writer = ReportWriterFactory.createProductionReportWriter(outputDirectory)

        writer.write(report, enableExtendedMode = false)
        writer.write(report, enableExtendedMode = true)

        LOGGER.info("Done!")
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