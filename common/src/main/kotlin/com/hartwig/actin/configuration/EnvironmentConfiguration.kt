package com.hartwig.actin.configuration

import com.fasterxml.jackson.databind.ObjectMapper
import com.fasterxml.jackson.dataformat.yaml.YAMLFactory
import com.fasterxml.jackson.module.kotlin.KotlinFeature
import com.fasterxml.jackson.module.kotlin.KotlinModule
import com.hartwig.actin.datamodel.molecular.evidence.Country
import org.apache.logging.log4j.LogManager
import java.io.File

enum class MolecularSummaryType {
    NONE,
    STANDARD
}

data class AlgoConfiguration(
    val warnIfToxicitiesNotFromQuestionnaire: Boolean = true,
    val maxMolecularTestAgeInDays: Int? = null
)

data class ClinicalConfiguration(
    val useOnlyPriorOtherConditions: Boolean = false
)

data class ReportConfiguration(
    val includeOverviewWithClinicalHistorySummary: Boolean = false,
    val includeMolecularDetailsChapter: Boolean = true,
    val includeSOCLiteratureEfficacyEvidence: Boolean = false,
    val includeEligibleSOCTreatmentSummary: Boolean = false,
    val molecularSummaryType: MolecularSummaryType = MolecularSummaryType.STANDARD,
    val includeOtherOncologicalHistoryInSummary: Boolean = true,
    val includePatientHeader: Boolean = true,
    val includeLesionsInTumorSummary: Boolean = true,
    val includePreviousPrimaryInClinicalSummary: Boolean = true,
    val includeRelevantNonOncologicalHistoryInSummary: Boolean = true,
    val includeApprovedTreatmentsInSummary: Boolean = true,
    val includeTrialMatchingInSummary: Boolean = true,
    val includeEligibleButNoSlotsTableIfEmpty: Boolean = true,
    val includeExternalTrialsInSummary: Boolean = true,
    val filterOnSOCExhaustionAndTumorType: Boolean = false,
    val includeClinicalDetailsChapter: Boolean = true,
    val includeTrialMatchingChapter: Boolean = true,
    val includeOnlyExternalTrialsInTrialMatching: Boolean = false,
    val includeLongitudinalMolecularChapter: Boolean = false,
    val includeMolecularEvidenceChapter: Boolean = false,
    val includeRawPathologyReport: Boolean = false,
    val includeTreatmentEvidenceRanking: Boolean = false,
    val countryOfReference: Country = Country.OTHER
)

const val OVERRIDE_YAML_ARGUMENT = "override_yaml"
const val OVERRIDE_YAML_DESCRIPTION = "Optional file specifying configuration overrides"

data class EnvironmentConfiguration(
    val requestingHospital: String? = null,
    val algo: AlgoConfiguration = AlgoConfiguration(),
    val report: ReportConfiguration = ReportConfiguration(),
    val clinical: ClinicalConfiguration = ClinicalConfiguration()
) {

    companion object {
        private val LOGGER = LogManager.getLogger(EnvironmentConfiguration::class.java)

        fun create(environmentConfigFile: String?): EnvironmentConfiguration {
            val configuration = environmentConfigFile?.let { readEnvironmentConfigYaml(it) } ?: EnvironmentConfiguration()
            val configSource = environmentConfigFile?.let { "file $it" } ?: "defaults"

            LOGGER.info("Loaded environment configuration from $configSource")
            return configuration
        }

        private fun readEnvironmentConfigYaml(environmentConfigYamlPath: String): EnvironmentConfiguration {
            val mapper = ObjectMapper(YAMLFactory())
            mapper.registerModules(KotlinModule.Builder().configure(KotlinFeature.NullIsSameAsDefault, true).build())
            mapper.findAndRegisterModules()
            return mapper.readValue(File(environmentConfigYamlPath), EnvironmentConfiguration::class.java)
        }
    }
}
