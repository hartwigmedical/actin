package com.hartwig.actin.configuration

import com.fasterxml.jackson.databind.ObjectMapper
import com.fasterxml.jackson.dataformat.yaml.YAMLFactory
import com.fasterxml.jackson.module.kotlin.KotlinFeature
import com.fasterxml.jackson.module.kotlin.KotlinModule
import com.hartwig.actin.molecular.datamodel.evidence.Country
import java.io.File
import org.apache.logging.log4j.LogManager

enum class ConfigurationProfile {
    STANDARD,
    CRC,
    MCGI
}

data class ReportConfiguration(
    val includeOverviewWithClinicalHistorySummary: Boolean = false,
    val includeMolecularDetailsChapter: Boolean = true,
    val includeIneligibleTrialsInSummary: Boolean = false,
    val includeSOCLiteratureEfficacyEvidence: Boolean = false,
    val includeEligibleSOCTreatmentSummary: Boolean = false,
    val includeMolecularSummary: Boolean = true,
    val includeOtherOncologicalHistoryInSummary: Boolean = true,
    val includePatientHeader: Boolean = true,
    val includeRelevantNonOncologicalHistoryInSummary: Boolean = true,
    val includeApprovedTreatmentsInSummary: Boolean = true,
    val includeTrialMatchingInSummary: Boolean = true,
    val includeExternalTrialsInSummary: Boolean = true,
    val filterOnSOCExhaustionAndTumorType: Boolean = false,
    val includeClinicalDetailsChapter: Boolean = true,
    val includeTrialMatchingChapter: Boolean = true,
    val includeOnlyExternalTrialsInTrialMatching: Boolean = false,
    val includeLongitudinalMolecularChapter: Boolean = false,
    val includeMolecularEvidenceChapter: Boolean = false,
    val countryOfReference: Country = Country.NETHERLANDS
)

const val EMC_TRIAL_SOURCE = "EMC"

data class AlgoConfiguration(
    val trialSource: String = EMC_TRIAL_SOURCE,
    val warnIfToxicitiesNotFromQuestionnaire: Boolean = true
)

data class TrialConfiguration(
    val ignoreAllNewTrialsInTrialStatusDatabase: Boolean = false,
)

const val OVERRIDE_YAML_ARGUMENT = "override_yaml"
const val OVERRIDE_YAML_DESCRIPTION = "Optional file specifying configuration overrides"

data class EnvironmentConfiguration(
    val algo: AlgoConfiguration = AlgoConfiguration(),
    val report: ReportConfiguration = ReportConfiguration(),
    val trial: TrialConfiguration = TrialConfiguration()
) {

    companion object {
        private val LOGGER = LogManager.getLogger(EnvironmentConfiguration::class.java)

        fun create(filePath: String?, profile: String? = null): EnvironmentConfiguration {
            val rawConfig = filePath?.let {
                val mapper = ObjectMapper(YAMLFactory())
                mapper.registerModules(KotlinModule.Builder().configure(KotlinFeature.NullIsSameAsDefault, true).build())
                mapper.findAndRegisterModules()
                mapper.readValue(File(filePath), EnvironmentConfiguration::class.java)
            } ?: EnvironmentConfiguration()

            val configProfile = profile?.let(ConfigurationProfile::valueOf) ?: ConfigurationProfile.STANDARD

            val configuration = when (configProfile) {
                ConfigurationProfile.CRC -> rawConfig.copy(
                    report = rawConfig.report.copy(
                        includeOverviewWithClinicalHistorySummary = true,
                        includeMolecularDetailsChapter = false,
                        includeIneligibleTrialsInSummary = true,
                        includeApprovedTreatmentsInSummary = false,
                        includeSOCLiteratureEfficacyEvidence = true,
                        includeEligibleSOCTreatmentSummary = true,
                        includeMolecularSummary = false,
                        includePatientHeader = false,
                        filterOnSOCExhaustionAndTumorType = true
                    )
                )

                ConfigurationProfile.MCGI -> rawConfig.copy(
                    report = rawConfig.report.copy(
                        includeMolecularDetailsChapter = false,
                        includeMolecularSummary = false,
                        includeApprovedTreatmentsInSummary = false,
                        includeTrialMatchingInSummary = false,
                        includeClinicalDetailsChapter = false,
                        includeTrialMatchingChapter = true,
                        includeOnlyExternalTrialsInTrialMatching = true,
                        includeExternalTrialsInSummary = false,
                        includeLongitudinalMolecularChapter = true,
                        includeMolecularEvidenceChapter = true,
                        countryOfReference = Country.US
                    )
                )

                ConfigurationProfile.STANDARD -> rawConfig
            }

            val configSource = filePath?.let { "file $it" } ?: "defaults"
            LOGGER.info("Loaded environment configuration from $configSource using $configProfile profile:\n$configuration")
            return configuration
        }
    }
}
