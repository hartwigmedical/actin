package com.hartwig.actin.clinical.curation.config

import com.hartwig.actin.clinical.curation.CurationDatabaseReader
import com.hartwig.actin.clinical.curation.TestCurationFactory
import org.assertj.core.api.Assertions.assertThat
import org.junit.Test

class MedicationNameConfigFactoryTest {
    private val fields: Map<String, Int> =
        TestCurationFactory.curationHeaders(CurationDatabaseReader.MEDICATION_NAME_TSV)

    @Test
    fun `Should return MedicationNameConfig from valid inputs`() {
        val config = MedicationNameConfigFactory().create(fields, arrayOf("input", "name"))
        assertThat(config.errors).isEmpty()
        assertThat(config.config.input).isEqualTo("input")
        assertThat(config.config.name).isEqualTo("name")
    }
}