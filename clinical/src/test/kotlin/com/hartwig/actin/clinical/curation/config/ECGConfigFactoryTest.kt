package com.hartwig.actin.clinical.curation.config

import com.hartwig.actin.clinical.curation.CurationCategory
import com.hartwig.actin.clinical.curation.CurationDatabaseReader
import com.hartwig.actin.clinical.curation.TestCurationFactory
import com.hartwig.actin.datamodel.clinical.Ecg
import com.hartwig.actin.datamodel.clinical.EcgMeasure
import org.assertj.core.api.Assertions.assertThat
import org.junit.Test


class ECGConfigFactoryTest {
    private val fields: Map<String, Int> = TestCurationFactory.curationHeaders(CurationDatabaseReader.ECG_TSV)

    @Test
    fun `Should return ECGConfig from valid inputs`() {
        val config = EcgConfigFactory().create(fields, arrayOf("input", "interpretation", "1", "1", "ms", "0", "", ""))
        assertThat(config.errors).isEmpty()
        assertThat(config.config.input).isEqualTo("input")
        assertThat(config.config.curated).isNotNull
        val curated = config.config.curated as Ecg
        with(curated) {
            assertThat(name).isEqualTo("interpretation")
            assertThat(qtcfMeasure).isEqualTo(EcgMeasure(1, "ms"))
            assertThat(jtcMeasure).isNull()
        }
    }

    @Test
    fun `Should return validation error when qtcf is not a number`() {
        val config = EcgConfigFactory().create(fields, arrayOf("input", "interpretation", "1", "invalid", "ms", "1", "1", "ms"))
        assertThat(config.errors).containsExactly(
            CurationConfigValidationError(
                CurationCategory.ECG.categoryName,
                "input",
                "qtcfValue",
                "invalid",
                "integer"
            )
        )
    }

    @Test
    fun `Should return validation error when jtc is not a number`() {
        val config = EcgConfigFactory().create(fields, arrayOf("input", "interpretation", "1", "1", "ms", "1", "invalid", "ms"))
        assertThat(config.errors).containsExactly(CurationConfigValidationError(
            CurationCategory.ECG.categoryName,
            "input",
            "jtcValue",
            "invalid",
            "integer"
        ))
    }
}