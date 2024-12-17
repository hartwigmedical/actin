package com.hartwig.actin.configuration

import com.fasterxml.jackson.databind.ObjectMapper
import com.fasterxml.jackson.dataformat.yaml.YAMLFactory
import com.fasterxml.jackson.module.kotlin.KotlinFeature
import com.fasterxml.jackson.module.kotlin.KotlinModule
import com.hartwig.actin.datamodel.molecular.evidence.CountryName
import org.apache.logging.log4j.LogManager
import java.io.File
import java.time.LocalDate

enum class ConfigurationProfile {
    STANDARD,
    CRC,
    MCGI
}

enum class MolecularSummaryType {
    NONE,
    STANDARD,
    SHORT;
}

data class AlgoConfiguration(
    val warnIfToxicitiesNotFromQuestionnaire: Boolean = true,
    val maxMolecularTestAgeInDays: Int? = null
)

data class TrialConfiguration(
    val ignoreAllNewTrialsInTrialStatusDatabase: Boolean = false,
)

data class ReportConfiguration(
    val includeOverviewWithClinicalHistorySummary: Boolean = false,
    val includeMolecularDetailsChapter: Boolean = true,
    val includeIneligibleTrialsInSummary: Boolean = false,
    val includeSOCLiteratureEfficacyEvidence: Boolean = false,
    val includeEligibleSOCTreatmentSummary: Boolean = false,
    val molecularSummaryType: MolecularSummaryType = MolecularSummaryType.STANDARD,
    val includeOtherOncologicalHistoryInSummary: Boolean = true,
    val includePatientHeader: Boolean = true,
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
    val countryOfReference: CountryName = CountryName.NETHERLANDS,
    val reportDate: LocalDate? = null
)

const val OVERRIDE_YAML_ARGUMENT = "override_yaml"
const val OVERRIDE_YAML_DESCRIPTION = "Optional file specifying configuration overrides"

data class EnvironmentConfiguration(
    val requestingHospital: String? = null,
    val algo: AlgoConfiguration = AlgoConfiguration(),
    val trial: TrialConfiguration = TrialConfiguration(),
    val report: ReportConfiguration = ReportConfiguration()
) {

    companion object {
        private val LOGGER = LogManager.getLogger(EnvironmentConfiguration::class.java)

        fun create(overridesPath: String?, profile: String? = null): EnvironmentConfiguration {
            val initialConfig = overridesPath?.let {
                val mapper = ObjectMapper(YAMLFactory())
                mapper.registerModules(KotlinModule.Builder().configure(KotlinFeature.NullIsSameAsDefault, true).build())
                mapper.findAndRegisterModules()
                mapper.readValue(File(overridesPath), EnvironmentConfiguration::class.java)
            } ?: EnvironmentConfiguration()

            val configProfile = profile?.let(ConfigurationProfile::valueOf) ?: ConfigurationProfile.STANDARD

            val configuration = when (configProfile) {
                ConfigurationProfile.CRC -> initialConfig.copy(
                    report = initialConfig.report.copy(
                        includeOverviewWithClinicalHistorySummary = true,
                        includeMolecularDetailsChapter = false,
                        includeIneligibleTrialsInSummary = true,
                        includeApprovedTreatmentsInSummary = false,
                        includeSOCLiteratureEfficacyEvidence = true,
                        includeEligibleSOCTreatmentSummary = true,
                        molecularSummaryType = MolecularSummaryType.NONE,
                        includePatientHeader = false,
                        filterOnSOCExhaustionAndTumorType = true
                    )
                )

                ConfigurationProfile.MCGI -> initialConfig.copy(
                    report = initialConfig.report.copy(
                        includeMolecularDetailsChapter = false,
                        molecularSummaryType = MolecularSummaryType.NONE,
                        includeApprovedTreatmentsInSummary = false,
                        includeTrialMatchingInSummary = false,
                        includeClinicalDetailsChapter = false,
                        includeTrialMatchingChapter = true,
                        includeOnlyExternalTrialsInTrialMatching = true,
                        includeExternalTrialsInSummary = false,
                        includeLongitudinalMolecularChapter = true,
                        includeMolecularEvidenceChapter = true,
                        countryOfReference = CountryName.US
                    )
                )

                ConfigurationProfile.STANDARD -> initialConfig
            }

            val configSource = overridesPath?.let { "file $it" } ?: "defaults"
            LOGGER.info("Loaded environment configuration from $configSource using $configProfile profile.")
            return configuration
        }
    }
}
