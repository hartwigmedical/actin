package com.hartwig.actin.clinical.curation.config

import com.hartwig.actin.clinical.curation.CurationCategory
import com.hartwig.actin.clinical.curation.CurationDatabaseReader
import com.hartwig.actin.clinical.curation.TestCurationFactory
import com.hartwig.actin.clinical.curation.datamodel.LesionLocationCategory
import org.assertj.core.api.Assertions.assertThat
import org.junit.Test

class LesionLocationConfigFactoryTest {
    private val fields: Map<String, Int> =
        TestCurationFactory.curationHeaders(CurationDatabaseReader.LESION_LOCATION_TSV)

    @Test
    fun `Should return LesionLocationConfig from valid inputs`() {
        val config = LesionLocationConfigFactory().create(fields, arrayOf("input", "location", "cns", "1"))
        assertThat(config.errors).isEmpty()
        assertThat(config.config.input).isEqualTo("input")
        assertThat(config.config.location).isEqualTo("location")
        assertThat(config.config.active).isEqualTo(true)
        assertThat(config.config.category).isEqualTo(LesionLocationCategory.CNS)
    }

    @Test
    fun `Should return validation error when category is not a value in enum`() {
        val config = LesionLocationConfigFactory().create(fields, arrayOf("input", "location", "hair", ""))
        assertThat(config.errors).containsExactly(
            CurationConfigValidationError(
                CurationCategory.LESION_LOCATION.categoryName,
                "input",
                "category",
                "hair",
                "LesionLocationCategory",
                "Accepted values are [BONE, LIVER, CNS, BRAIN, LUNG, LYMPH_NODE]"
            )
        )
    }
}