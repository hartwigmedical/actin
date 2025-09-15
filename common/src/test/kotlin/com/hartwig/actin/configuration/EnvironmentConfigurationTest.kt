package com.hartwig.actin.configuration

import com.hartwig.actin.testutil.ResourceLocator.resourceOnClasspath
import org.assertj.core.api.Assertions.assertThat
import org.junit.Test

class EnvironmentConfigurationTest {
    
    private val minimalConfigFile = resourceOnClasspath("environment/minimal_config.yaml")
    private val properConfigFile = resourceOnClasspath("environment/proper_config.yaml")
    
    @Test
    fun `Should load proper config from file`() {
        val reportConfig = ReportConfiguration.create(properConfigFile)
        assertThat(reportConfig.hospitalOfReference).isEqualTo("Erasmus MC")

        val algoConfig = AlgoConfiguration.create(properConfigFile)
        assertThat(algoConfig.warnIfToxicitiesNotFromQuestionnaire).isFalse()
    }
    
    @Test
    fun `Should use defaults for fields not provided in file`() {
        val reportConfig = ReportConfiguration.create(minimalConfigFile)
        assertThat(reportConfig.hospitalOfReference).isNull()

        val algoConfig = AlgoConfiguration.create(minimalConfigFile)
        assertThat(algoConfig.warnIfToxicitiesNotFromQuestionnaire).isTrue()
    }

    @Test
    fun `Should create default configuration when provided file is null`() {
        assertThat(ReportConfiguration.create(null)).isEqualTo(ReportConfiguration())
        assertThat(AlgoConfiguration.create(null)).isEqualTo(AlgoConfiguration())
    }
}