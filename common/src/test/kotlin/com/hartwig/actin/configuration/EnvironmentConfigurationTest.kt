package com.hartwig.actin.configuration

import com.hartwig.actin.testutil.ResourceLocator.resourceOnClasspath
import org.assertj.core.api.Assertions.assertThat
import org.junit.Test

class EnvironmentConfigurationTest {

    private val defaultConfig = EnvironmentConfiguration()
    private val minimalConfigFile = resourceOnClasspath("environment/minimal_config.yaml")
    private val properConfigFile = resourceOnClasspath("environment/proper_config.yaml")
    
    @Test
    fun `Should load proper config from file`() {
        val reportConfig = EnvironmentConfiguration.createReportConfig(properConfigFile)
        assertThat(reportConfig.hospitalOfReference).isEqualTo("Erasmus MC")

        val algoConfig = EnvironmentConfiguration.createAlgoConfig(properConfigFile)
        assertThat(algoConfig.warnIfToxicitiesNotFromQuestionnaire).isFalse()
    }
    
    @Test
    fun `Should use defaults for fields not provided in file`() {
        val reportConfig = EnvironmentConfiguration.createReportConfig(minimalConfigFile)
        assertThat(reportConfig.hospitalOfReference).isNull()

        val algoConfig = EnvironmentConfiguration.createAlgoConfig(minimalConfigFile)
        assertThat(algoConfig.warnIfToxicitiesNotFromQuestionnaire).isTrue()
    }

    @Test
    fun `Should create default configuration when provided file is null`() {
        assertThat(EnvironmentConfiguration.createReportConfig(null)).isEqualTo(defaultConfig.report)
        assertThat(EnvironmentConfiguration.createAlgoConfig(null)).isEqualTo(defaultConfig.algo)
    }
}