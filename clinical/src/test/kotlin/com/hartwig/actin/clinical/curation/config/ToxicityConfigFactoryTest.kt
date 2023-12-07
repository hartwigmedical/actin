package com.hartwig.actin.clinical.curation.config

import com.hartwig.actin.clinical.curation.CURATION_DIRECTORY
import com.hartwig.actin.clinical.curation.CurationDatabaseReader
import org.assertj.core.api.Assertions.assertThat
import org.junit.Test

class ToxicityConfigFactoryTest {
    private val fields: Map<String, Int> = CurationConfigFile.readTsv(CURATION_DIRECTORY + CurationDatabaseReader.TOXICITY_TSV).second

    @Test
    fun `Should return ToxicityConfig from valid inputs`() {
        val config = ToxicityConfigFactory().create(fields, arrayOf("input", "name", "categories", "3"))
        assertThat(config.errors).isEmpty()
        assertThat(config.config.input).isEqualTo("input")
        assertThat(config.config.name).isEqualTo("name")
        assertThat(config.config.categories).containsExactly("categories")
        assertThat(config.config.grade).isEqualTo(3)
    }
}