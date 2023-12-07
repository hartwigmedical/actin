package com.hartwig.actin.clinical.curation.config

import com.hartwig.actin.clinical.curation.CURATION_DIRECTORY
import com.hartwig.actin.clinical.curation.CurationDatabaseReader
import org.assertj.core.api.Assertions.assertThat
import org.junit.Test

class InfectionConfigFactoryTest {
    private val fields: Map<String, Int> = CurationConfigFile.readTsv(CURATION_DIRECTORY + CurationDatabaseReader.INFECTION_TSV).second

    @Test
    fun `Should return InfectionConfig from valid inputs`() {
        val config = InfectionConfigFactory().create(fields, arrayOf("input", "interpretation"))
        assertThat(config.errors).isEmpty()
        assertThat(config.config.input).isEqualTo("input")
        assertThat(config.config.interpretation).isEqualTo("interpretation")
    }
}