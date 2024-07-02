package com.hartwig.actin.configuration

import com.fasterxml.jackson.databind.ObjectMapper
import com.fasterxml.jackson.dataformat.yaml.YAMLFactory
import com.fasterxml.jackson.module.kotlin.KotlinFeature
import com.fasterxml.jackson.module.kotlin.KotlinModule
import java.io.File
import org.apache.logging.log4j.LogManager

enum class ConfigurationProfile {
    STANDARD,
    CRC
}

data class ReportConfiguration(
    val includeOverviewWithClinicalHistorySummary: Boolean = false,
    val includeMolecularDetailsChapter: Boolean = true,
    val showIneligibleTrialsInSummary: Boolean = false,
    val showSOCLiteratureEfficacyEvidence: Boolean = false,
    val showEligibleSOCTreatmentSummary: Boolean = false,
    val showMolecularSummary: Boolean = true,
    val showOtherOncologicalHistoryInSummary: Boolean = true,
    val showPatientHeader: Boolean = true,
    val showRelevantNonOncologicalHistoryInSummary: Boolean = true,
    val showApprovedTreatmentsInSummary: Boolean = true
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
                        showIneligibleTrialsInSummary = true,
                        showApprovedTreatmentsInSummary = false,
                        showSOCLiteratureEfficacyEvidence = true,
                        showEligibleSOCTreatmentSummary = true,
                        showMolecularSummary = false,
                        showPatientHeader = false
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
