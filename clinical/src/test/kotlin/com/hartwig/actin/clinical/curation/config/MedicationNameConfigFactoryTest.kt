package com.hartwig.actin.clinical.curation.config

import com.hartwig.actin.clinical.curation.CURATION_DIRECTORY
import com.hartwig.actin.clinical.curation.CurationDatabaseReader
import com.hartwig.actin.clinical.datamodel.Dosage
import org.assertj.core.api.Assertions.assertThat
import org.junit.Test

class MedicationNameConfigFactoryTest {
    private val fields: Map<String, Int> =
        CurationConfigFile.readTsv(CURATION_DIRECTORY + CurationDatabaseReader.MEDICATION_NAME_TSV).second

    @Test
    fun `Should return MedicationNameConfig from valid inputs`() {
        val config = MedicationNameConfigFactory().create(fields, arrayOf("input", "name"))
        assertThat(config.errors).isEmpty()
        assertThat(config.config.input).isEqualTo("input")
        assertThat(config.config.name).isEqualTo("name")
    }
}