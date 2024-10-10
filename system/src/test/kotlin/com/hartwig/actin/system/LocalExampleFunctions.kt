package com.hartwig.actin.system

import com.hartwig.actin.configuration.AlgoConfiguration
import com.hartwig.actin.configuration.EnvironmentConfiguration
import com.hartwig.actin.configuration.ReportConfiguration
import java.io.File

object LocalExampleFunctions {

    private const val TRIAL_SOURCE = "Example"

    fun createExampleEnvironmentConfiguration(): EnvironmentConfiguration {
        val base = EnvironmentConfiguration.create(null)
        return base.copy(
            algo = AlgoConfiguration(trialSource = TRIAL_SOURCE),
            report = ReportConfiguration(
                includeApprovedTreatmentsInSummary = false,
                includeExternalTrialsInSummary = false,
                includeMolecularDetailsChapter = false,
                includeClinicalDetailsChapter = false
            )
        )
    }

    fun systemTestResourcesDirectory(): String {
        return listOf(
            System.getProperty("user.home"),
            "hmf",
            "repos",
            "actin",
            "system",
            "src",
            "test",
            "resources"
        ).joinToString(File.separator)
    }
}