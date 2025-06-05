package com.hartwig.actin.clinical.curation.config

import com.hartwig.actin.clinical.curation.CurationDatabaseReader
import com.hartwig.actin.clinical.curation.TestCurationFactory
import com.hartwig.actin.datamodel.clinical.SurgeryType
import com.hartwig.actin.datamodel.clinical.ingestion.CurationCategory
import org.assertj.core.api.Assertions.assertThat
import org.junit.Test

class SurgeryConfigFactoryTest {

    private val fields: Map<String, Int> = TestCurationFactory.curationHeaders(CurationDatabaseReader.SURGERY_TSV)

    @Test
    fun `Should return surgery config from valid inputs`() {
        val surgeryConfig = SurgeryConfigFactory().create(fields, arrayOf("dikke", "surg_1", "CYTOREDUCTIVE_SURGERY"))
        assertThat(surgeryConfig.errors).isEmpty()
        assertThat(surgeryConfig.config.input).isEqualTo("dikke")
        assertThat(surgeryConfig.config.name).isEqualTo("surg_1")
        assertThat(surgeryConfig.config.type).isEqualTo(SurgeryType.CYTOREDUCTIVE_SURGERY)
    }

    @Test
    fun `Should return curation config validation error if unknown enum`() {
        val surgeryConfig = SurgeryConfigFactory().create(fields, arrayOf("dikke", "surg_1", "UNKNOWN_ENUM"))
        assertThat(surgeryConfig.config.type).isEqualTo(SurgeryType.UNKNOWN)
        assertThat(surgeryConfig.errors).isNotEmpty
        assertThat(surgeryConfig.errors.first().category).isEqualTo(CurationCategory.SURGERY)
        assertThat(surgeryConfig.errors.first().invalidValue).isEqualTo("UNKNOWN_ENUM")
        assertThat(surgeryConfig.errors.first().validType).isEqualTo("SurgeryType")
        assertThat(surgeryConfig.errors.first().additionalMessage).isEqualTo("Accepted values are [CYTOREDUCTIVE_SURGERY, DEBULKING_SURGERY, UNKNOWN]")
    }
}