package com.hartwig.actin.configuration

import com.hartwig.actin.testutil.ResourceLocator
import org.assertj.core.api.Assertions.assertThat
import org.junit.Test

class EnvironmentConfigurationTest {
    private val locateResource = ResourceLocator(this)

    @Test
    fun `Should create with default values`() {
        assertThat(EnvironmentConfiguration().algo.warnIfToxicitiesNotFromQuestionnaire).isTrue
        assertThat(EnvironmentConfiguration().report.showClinicalSummary).isTrue
    }

    @Test
    fun `Should load config from file`() {
        val config = EnvironmentConfiguration.createFromFile(locateResource.onClasspath("/config.yaml"))
        assertThat(config.algo.warnIfToxicitiesNotFromQuestionnaire).isFalse
        assertThat(config.report.showClinicalSummary).isFalse
    }

    @Test
    fun `Should use defaults for fields not provided in file`() {
        val config = EnvironmentConfiguration.createFromFile(locateResource.onClasspath("/minimal_config.yaml"))
        assertThat(config.algo.warnIfToxicitiesNotFromQuestionnaire).isTrue
        assertThat(config.report.showClinicalSummary).isTrue
    }
}