package com.hartwig.actin.clinical.curation.config

import com.hartwig.actin.clinical.curation.CURATION_DIRECTORY
import com.hartwig.actin.clinical.curation.CurationDatabaseReader
import com.hartwig.actin.clinical.curation.datamodel.LesionLocationCategory
import org.assertj.core.api.Assertions.assertThat
import org.junit.Test

class LesionLocationConfigFactoryTest {
    private val fields: Map<String, Int> =
        CurationConfigFile.readTsv(CURATION_DIRECTORY + CurationDatabaseReader.LESION_LOCATION_TSV).second

    @Test
    fun `Should return LesionLocationConfig from valid inputs`() {
        val config = LesionLocationConfigFactory().create(fields, arrayOf("input", "location", "cns"))
        assertThat(config.errors).isEmpty()
        assertThat(config.config.input).isEqualTo("input")
        assertThat(config.config.location).isEqualTo("location")
        assertThat(config.config.category).isEqualTo(LesionLocationCategory.CNS)
    }

    @Test
    fun `Should return validation error when category is not a value in enum`() {
        val config = LesionLocationConfigFactory().create(fields, arrayOf("input", "location", "hair"))
        assertThat(config.errors).containsExactly(
            CurationConfigValidationError(
                "Invalid enum value 'HAIR' for enum 'LesionLocationCategory'. Accepted values are [BONE, LIVER, CNS, BRAIN, LUNG, LYMPH_NODE]"
            )
        )
    }
}