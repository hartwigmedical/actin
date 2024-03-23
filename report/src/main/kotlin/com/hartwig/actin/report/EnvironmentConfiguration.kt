package com.hartwig.actin.report

import com.fasterxml.jackson.databind.ObjectMapper
import com.fasterxml.jackson.dataformat.yaml.YAMLFactory
import java.io.File

data class ReportConfiguration(
    val showClinicalSummary: Boolean = true
)

data class EnvironmentConfiguration(
    val report: ReportConfiguration = ReportConfiguration()
) {

    companion object {
        fun createFromFile(filePath: String): EnvironmentConfiguration {
            val mapper = ObjectMapper(YAMLFactory())
            mapper.configure(com.fasterxml.jackson.databind.DeserializationFeature.FAIL_ON_UNKNOWN_PROPERTIES, false)
            mapper.findAndRegisterModules()
            return mapper.readValue(File(filePath), EnvironmentConfiguration::class.java)
        }
    }
}
