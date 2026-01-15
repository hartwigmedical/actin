package com.hartwig.actin.configuration

import com.hartwig.actin.testutil.ResourceLocator.resourceOnClasspath
import org.assertj.core.api.Assertions.assertThat
import org.junit.Test

class EnvironmentConfigurationTest {
    
    private val minimalConfigFile = resourceOnClasspath("environment/minimal_config.yaml")
    private val properConfigFile = resourceOnClasspath("environment/proper_config.yaml")
    
    @Test
    fun `Should load proper config from file`() {
        val molecularConfig = MolecularConfiguration.create(properConfigFile)
        assertThat(molecularConfig.eventPathogenicityIsConfirmed).isTrue()

        val algoConfig = AlgoConfiguration.create(properConfigFile)
        assertThat(algoConfig.warnIfToxicitiesNotFromQuestionnaire).isFalse()

        val reportConfig = ReportConfiguration.create(properConfigFile)
        assertThat(reportConfig.hospitalOfReference).isEqualTo("Erasmus MC")
    }
    
    @Test
    fun `Should use defaults for fields not provided in file`() {
        val molecularConfig = MolecularConfiguration.create(minimalConfigFile)
        assertThat(molecularConfig.eventPathogenicityIsConfirmed).isFalse()

        val algoConfig = AlgoConfiguration.create(minimalConfigFile)
        assertThat(algoConfig.warnIfToxicitiesNotFromQuestionnaire).isTrue()

        val reportConfig = ReportConfiguration.create(minimalConfigFile)
        assertThat(reportConfig.hospitalOfReference).isNull()
    }

    @Test
    fun `Should create default configuration when provided file is null`() {
        assertThat(MolecularConfiguration.create(null)).isEqualTo(MolecularConfiguration())
        assertThat(AlgoConfiguration.create(null)).isEqualTo(AlgoConfiguration())
        assertThat(ReportConfiguration.create(null)).isEqualTo(ReportConfiguration())
    }
}