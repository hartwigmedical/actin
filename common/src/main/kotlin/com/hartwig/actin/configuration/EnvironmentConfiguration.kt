package com.hartwig.actin.configuration

import com.fasterxml.jackson.databind.ObjectMapper
import com.fasterxml.jackson.dataformat.yaml.YAMLFactory
import com.fasterxml.jackson.module.kotlin.KotlinFeature
import com.fasterxml.jackson.module.kotlin.KotlinModule
import org.apache.logging.log4j.LogManager
import java.io.File

enum class ConfigurationProfile {
    STANDARD,
    CRC
}

data class ReportConfiguration(
    val filterTrialsWithOverlappingMolecularTargetsInSummary: Boolean = false,
    val includeOverviewWithClinicalHistorySummary: Boolean = false,
    val includeMolecularChapter: Boolean = true,
    val showEfficacy: Boolean = false,
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

const val OVERRIDE_YAML_ARGUMENT = "override_yaml"
const val OVERRIDE_YAML_DESCRIPTION = "Optional file specifying configuration overrides"

data class EnvironmentConfiguration(
    val algo: AlgoConfiguration = AlgoConfiguration(),
    val report: ReportConfiguration = ReportConfiguration()
) {

    companion object {
        private val LOGGER = LogManager.getLogger(EnvironmentConfiguration::class.java)

        fun createFromFile(filePath: String, profile: String? = null): EnvironmentConfiguration {
            val configProfile = profile?.let { ConfigurationProfile.valueOf(it) } ?: ConfigurationProfile.STANDARD
            val mapper = ObjectMapper(YAMLFactory())
            mapper.registerModules(KotlinModule.Builder().configure(KotlinFeature.NullIsSameAsDefault, true).build())
            mapper.findAndRegisterModules()

            val rawConfig = mapper.readValue(File(filePath), EnvironmentConfiguration::class.java)
            val configuration = when (configProfile) {
                ConfigurationProfile.CRC -> rawConfig.copy(
                    report = rawConfig.report.copy(
                        includeOverviewWithClinicalHistorySummary = true,
                        includeMolecularChapter = false,
                        showApprovedTreatmentsInSummary = false,
                        showEfficacy = true,
                        showEligibleSOCTreatmentSummary = true,
                        showMolecularSummary = false,
                        showPatientHeader = false
                    )
                )

                ConfigurationProfile.STANDARD -> rawConfig
            }
            LOGGER.info("Loaded environment configuration from file $filePath:\n$configuration")
            return configuration
        }
    }
}
