package com.hartwig.actin.clinical.curation.config

import com.hartwig.actin.clinical.curation.CurationDatabaseReader
import com.hartwig.actin.clinical.curation.TestCurationFactory
import org.assertj.core.api.Assertions.assertThat
import org.junit.Test

class ToxicityConfigFactoryTest {
    private val fields: Map<String, Int> = TestCurationFactory.curationHeaders(CurationDatabaseReader.TOXICITY_TSV)

    @Test
    fun `Should return ToxicityConfig from valid inputs`() {
        val config = ToxicityConfigFactory().create(fields, arrayOf("input", "name", "categories", "3"))
        assertThat(config.errors).isEmpty()
        assertThat(config.config.input).isEqualTo("input")
        assertThat(config.config.name).isEqualTo("name")
        assertThat(config.config.categories).containsExactly("categories")
        assertThat(config.config.grade).isEqualTo(3)
    }

    @Test
    fun `Should return validation error when grade is not an integer`() {
        val config = ToxicityConfigFactory().create(fields, arrayOf("input", "name", "categories", "abc"))
        assertThat(config.errors).containsExactly(CurationConfigValidationError("'grade' had invalid value of 'abc' for input 'input'"))
    }
}