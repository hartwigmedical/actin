package com.hartwig.actin.configuration

import com.fasterxml.jackson.databind.ObjectMapper
import com.fasterxml.jackson.dataformat.yaml.YAMLFactory
import com.fasterxml.jackson.module.kotlin.KotlinFeature
import com.fasterxml.jackson.module.kotlin.KotlinModule
import java.io.File

data class ReportConfiguration(
    val showClinicalSummary: Boolean = true,
    val localTrialThreshold: Int = 5
)

const val EMC_TRIAL_SOURCE = "EMC"

data class AlgoConfiguration(
    val trialSource: String = EMC_TRIAL_SOURCE
)

const val OVERRIDE_YAML_ARGUMENT = "override_yaml"
const val OVERRIDE_YAML_DESCRIPTION = "Optional file specifying configuration overrides"

data class EnvironmentConfiguration(
    val report: ReportConfiguration = ReportConfiguration(),
    val algo: AlgoConfiguration = AlgoConfiguration()
) {

    companion object {

        fun createFromFile(filePath: String): EnvironmentConfiguration {
            val mapper = ObjectMapper(YAMLFactory())
            mapper.registerModules(KotlinModule.Builder().configure(KotlinFeature.NullIsSameAsDefault, true).build())
            mapper.findAndRegisterModules()
            return mapper.readValue(File(filePath), EnvironmentConfiguration::class.java)
        }
    }
}
