package com.hartwig.actin.report

import com.fasterxml.jackson.databind.ObjectMapper
import com.fasterxml.jackson.dataformat.yaml.YAMLFactory
import com.fasterxml.jackson.module.kotlin.KotlinFeature
import com.fasterxml.jackson.module.kotlin.KotlinModule
import java.io.File

data class ReportConfiguration(
    val showClinicalSummary: Boolean = true,
    val showMolecularSummary: Boolean = true,
    val showOtherOncologicalHistoryInSummary: Boolean = true,
    val showRelevantNonOncologicalHistoryInSummary: Boolean = true,
    val showApprovedTreatmentsInSummary: Boolean = true
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
