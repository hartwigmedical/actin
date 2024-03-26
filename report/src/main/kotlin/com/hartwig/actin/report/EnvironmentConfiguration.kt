package com.hartwig.actin.report

import com.fasterxml.jackson.databind.ObjectMapper
import com.fasterxml.jackson.dataformat.yaml.YAMLFactory
import com.fasterxml.jackson.module.kotlin.KotlinFeature
import com.fasterxml.jackson.module.kotlin.KotlinModule
import java.io.File

data class ReportConfiguration(
    val showClinicalSummary: Boolean = true,
    val showMolecularSummary: Boolean = true,
    val showApprovedTreatments: Boolean = true,
    val showOtherOncologicalHistory: Boolean = true,
    val showPreviousPrimaryTumor: Boolean = true,
    val showRelevantNonOncologicalHistory: Boolean = true
)

data class EnvironmentConfiguration(
    val report: ReportConfiguration = ReportConfiguration()
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
