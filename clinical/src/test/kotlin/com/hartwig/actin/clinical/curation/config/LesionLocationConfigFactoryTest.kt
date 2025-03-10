package com.hartwig.actin.clinical.curation.config

import com.hartwig.actin.datamodel.clinical.ingestion.CurationCategory
import com.hartwig.actin.clinical.curation.CurationDatabaseReader
import com.hartwig.actin.clinical.curation.TestCurationFactory
import com.hartwig.actin.clinical.curation.datamodel.LesionLocationCategory
import com.hartwig.actin.datamodel.clinical.ingestion.CurationConfigValidationError
import org.assertj.core.api.Assertions.assertThat
import org.junit.Test

class LesionLocationConfigFactoryTest {
    private val fields: Map<String, Int> =
        TestCurationFactory.curationHeaders(CurationDatabaseReader.LESION_LOCATION_TSV)

    @Test
    fun `Should return LesionLocationConfig from valid inputs`() {
        val lesionConfig = LesionLocationConfigFactory().create(fields, arrayOf("input", "location", "cns", "1", "1"))
        assertThat(lesionConfig.errors).isEmpty()
        assertThat(lesionConfig.config.input).isEqualTo("input")
        assertThat(lesionConfig.config.location).isEqualTo("location")
        assertThat(lesionConfig.config.active).isEqualTo(true)
        assertThat(lesionConfig.config.suspected).isEqualTo(true)
        assertThat(lesionConfig.config.category).isEqualTo(LesionLocationCategory.CNS)
    }

    @Test
    fun `Should return validation error when category is not a value in enum`() {
        val lesionConfig = LesionLocationConfigFactory().create(fields, arrayOf("input", "location", "hair", "", ""))
        assertThat(lesionConfig.errors).containsExactly(
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