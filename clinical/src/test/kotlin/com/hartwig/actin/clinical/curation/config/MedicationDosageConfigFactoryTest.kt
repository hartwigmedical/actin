package com.hartwig.actin.clinical.curation.config

import com.hartwig.actin.clinical.curation.CURATION_DIRECTORY
import com.hartwig.actin.clinical.curation.CurationDatabaseReader
import com.hartwig.actin.clinical.datamodel.Dosage
import org.assertj.core.api.Assertions.assertThat
import org.junit.Test

class MedicationDosageConfigFactoryTest {
    private val fields: Map<String, Int> =
        CurationConfigFile.readTsv(CURATION_DIRECTORY + CurationDatabaseReader.MEDICATION_DOSAGE_TSV).second

    @Test
    fun `Should return MedicationDosageConfig from valid inputs`() {
        val config = MedicationDosageConfigFactory().create(fields, arrayOf("input", "1", "2", "ml", "1", "day", "2", "day", "1"))
        assertThat(config.errors).isEmpty()
        assertThat(config.config.input).isEqualTo("input")
        assertThat(config.config.curated.dosageMin()).isEqualTo(1.0)
        assertThat(config.config.curated.dosageMax()).isEqualTo(2.0)
        assertThat(config.config.curated.dosageUnit()).isEqualTo("ml")
        assertThat(config.config.curated.frequency()).isEqualTo(1.0)
        assertThat(config.config.curated.frequencyUnit()).isEqualTo("day")
        assertThat(config.config.curated.periodBetweenValue()).isEqualTo(2.0)
        assertThat(config.config.curated.periodBetweenUnit()).isEqualTo("day")
        assertThat(config.config.curated.ifNeeded()).isEqualTo(true)
    }
}