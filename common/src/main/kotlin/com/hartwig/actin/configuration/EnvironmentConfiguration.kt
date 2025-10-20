package com.hartwig.actin.configuration

import com.fasterxml.jackson.databind.ObjectMapper
import com.fasterxml.jackson.dataformat.yaml.YAMLFactory
import com.fasterxml.jackson.module.kotlin.KotlinFeature
import com.fasterxml.jackson.module.kotlin.KotlinModule
import org.apache.logging.log4j.LogManager
import java.io.File

const val OVERRIDE_YAML_ARGUMENT = "override_yaml"
const val OVERRIDE_YAML_DESCRIPTION = "Optional file specifying configuration overrides"
 
data class EnvironmentConfiguration(
    val algo: AlgoConfiguration = AlgoConfiguration(),
    val report: ReportConfiguration = ReportConfiguration()
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
