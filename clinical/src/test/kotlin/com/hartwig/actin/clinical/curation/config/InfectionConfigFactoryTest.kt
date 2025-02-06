package com.hartwig.actin.clinical.curation.config

import com.hartwig.actin.clinical.curation.CurationDatabaseReader
import com.hartwig.actin.clinical.curation.TestCurationFactory
import com.hartwig.actin.icd.TestIcdFactory
import org.assertj.core.api.Assertions.assertThat
import org.junit.Test

class InfectionConfigFactoryTest {
    private val fields: Map<String, Int> = TestCurationFactory.curationHeaders(CurationDatabaseReader.INFECTION_TSV)

    @Test
    fun `Should return InfectionConfig from valid inputs`() {
        val (config, errors) = InfectionConfigFactory(TestIcdFactory.createTestModel()).create(fields, arrayOf("input", "interpretation"))
        assertThat(errors).isEmpty()
        assertThat(config.input).isEqualTo("input")
        assertThat(config.ignore).isFalse()
        assertThat(config.curated).isNotNull
        assertThat(config.curated!!.name).isEqualTo("interpretation")
    }
}