package com.hartwig.actin.clinical.curation.config

import com.hartwig.actin.clinical.curation.CurationDatabaseReader
import com.hartwig.actin.clinical.curation.TestCurationFactory
import com.hartwig.actin.datamodel.clinical.SurgeryType
import com.hartwig.actin.datamodel.clinical.ingestion.CurationCategory
import org.assertj.core.api.Assertions.assertThat
import org.junit.Test

class SurgeryNameConfigFactoryTest {

    private val fields: Map<String, Int> =
        TestCurationFactory.curationHeaders(CurationDatabaseReader.SURGERY_NAME_TSV)

    @Test
    fun `Should return SurgeryNameConfig from valid inputs`() {
        val lesionConfig = SurgeryNameConfigFactory().create(fields, arrayOf("dikke", "surg_1", "CYTOREDUCTIVE_SURGERY"))
        assertThat(lesionConfig.errors).isEmpty()
        assertThat(lesionConfig.config.input).isEqualTo("dikke")
        assertThat(lesionConfig.config.name).isEqualTo("surg_1")
        assertThat(lesionConfig.config.type).isEqualTo(SurgeryType.CYTOREDUCTIVE_SURGERY)
    }

    @Test
    fun `Should return curation config validation error if unknown enum`() {
        val lesionConfig = SurgeryNameConfigFactory().create(fields, arrayOf("dikke", "surg_1", "UNKNOWN_ENUM"))
        assertThat(lesionConfig.errors).isNotEmpty
        assertThat(lesionConfig.errors.first().category).isEqualTo(CurationCategory.SURGERY_NAME)
        assertThat(lesionConfig.errors.first().invalidValue).isEqualTo("UNKNOWN_ENUM")
        assertThat(lesionConfig.errors.first().validType).isEqualTo("SurgeryType")
        assertThat(lesionConfig.errors.first().additionalMessage).isEqualTo("Accepted values are [CYTOREDUCTIVE_SURGERY, DEBULKING_SURGERY]")
    }
}