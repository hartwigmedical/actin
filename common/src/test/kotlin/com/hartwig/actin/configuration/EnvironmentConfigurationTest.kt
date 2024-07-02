package com.hartwig.actin.configuration

import com.hartwig.actin.testutil.ResourceLocator.resourceOnClasspath
import org.assertj.core.api.Assertions.assertThat
import org.junit.Test

class EnvironmentConfigurationTest {
    private val defaultConfig = EnvironmentConfiguration()
    
    @Test
    fun `Should create with default values`() {
        assertThat(defaultConfig.algo.warnIfToxicitiesNotFromQuestionnaire).isTrue
    }

    @Test
    fun `Should load config from file`() {
        val config = EnvironmentConfiguration.create(resourceOnClasspath("/config.yaml"))
        assertThat(config.algo.warnIfToxicitiesNotFromQuestionnaire).isFalse
    }

    @Test
    fun `Should use defaults for fields not provided in file`() {
        val config = EnvironmentConfiguration.create(resourceOnClasspath("/minimal_config.yaml"))
        assertThat(config.algo.warnIfToxicitiesNotFromQuestionnaire).isTrue
    }

    @Test
    fun `Should create default configuration when provided file is null`() {
        assertThat(EnvironmentConfiguration.create(null)).isEqualTo(defaultConfig)
    }

    @Test
    fun `Should override configuration from file with profile`() {
        val config = EnvironmentConfiguration.create(resourceOnClasspath("/config.yaml"), "CRC")
        assertThat(config.algo.warnIfToxicitiesNotFromQuestionnaire).isFalse
        assertThat(config.report.includeOverviewWithClinicalHistorySummary).isTrue
        assertThat(config.report.includeMolecularDetailsChapter).isFalse
    }

    @Test
    fun `Should override default configuration with profile when file is null`() {
        assertThat(EnvironmentConfiguration.create(null, "CRC")).isEqualTo(
            defaultConfig.copy(
                report = defaultConfig.report.copy(
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
        )
    }
}