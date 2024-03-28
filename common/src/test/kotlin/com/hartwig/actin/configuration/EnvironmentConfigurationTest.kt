package com.hartwig.actin.configuration

import org.assertj.core.api.Assertions.assertThat
import org.junit.Test

class EnvironmentConfigurationTest {

    @Test
    fun `Should create with default values`() {
        assertThat(EnvironmentConfiguration().report.showClinicalSummary).isTrue
    }

    @Test
    fun `Should load config from file`() {
        val configFilePath = EnvironmentConfigurationTest::class.java.getResource("/config.yaml")!!.path
        assertThat(EnvironmentConfiguration.createFromFile(configFilePath).report.showClinicalSummary).isFalse
    }

    @Test
    fun `Should use defaults for fields not provided in file`() {
        val configFilePath = EnvironmentConfigurationTest::class.java.getResource("/minimal_config.yaml")!!.path
        val config = EnvironmentConfiguration.createFromFile(configFilePath)
        assertThat(config.report.showClinicalSummary).isTrue
    }
}