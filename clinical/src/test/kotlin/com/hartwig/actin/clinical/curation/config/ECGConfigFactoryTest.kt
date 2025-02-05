package com.hartwig.actin.clinical.curation.config

import com.hartwig.actin.clinical.curation.CurationCategory
import com.hartwig.actin.clinical.curation.CurationDatabaseReader
import com.hartwig.actin.clinical.curation.TestCurationFactory
import com.hartwig.actin.datamodel.clinical.Ecg
import com.hartwig.actin.datamodel.clinical.EcgMeasure
import org.assertj.core.api.Assertions.assertThat
import org.junit.Test


private const val INPUT = "input"

class ECGConfigFactoryTest {
    private val fields: Map<String, Int> = TestCurationFactory.curationHeaders(CurationDatabaseReader.ECG_TSV)

    @Test
    fun `Should return ECG config from valid inputs`() {
        val config = EcgConfigFactory().create(fields, arrayOf(INPUT, "interpretation", "1", "1", "ms", "0", "", ""))
        assertThat(config.errors).isEmpty()
        assertThat(config.config.input).isEqualTo(INPUT)
        assertThat(config.config.curated).isNotNull
        val curated = config.config.curated as Ecg
        with(curated) {
            assertThat(name).isEqualTo("interpretation")
            assertThat(qtcfMeasure).isEqualTo(EcgMeasure(1, "ms"))
            assertThat(jtcMeasure).isNull()
        }
    }

    @Test
    fun `Should return ignore config when input evaluates to false`() {
        val config = EcgConfigFactory().create(fields, arrayOf("nvt", "", "", "", "", "", "", ""))
        assertThat(config.errors).isEmpty()
        assertThat(config.config).isEqualTo(ComorbidityConfig("nvt", ignore = true, curated = null))
    }

    @Test
    fun `Should return ignore config when input evaluates to null`() {
        val config = EcgConfigFactory().create(fields, arrayOf("possible", "", "", "", "", "", "", ""))
        assertThat(config.errors).isEmpty()
        assertThat(config.config).isEqualTo(ComorbidityConfig("possible", ignore = true, curated = null))
    }

    @Test
    fun `Should return ignore config when interpretation is NULL`() {
        val config = EcgConfigFactory().create(fields, arrayOf(INPUT, "NULL", "", "", "", "", "", ""))
        assertThat(config.errors).isEmpty()
        assertThat(config.config).isEqualTo(ComorbidityConfig(INPUT, ignore = true, curated = null))
    }

    @Test
    fun `Should return empty config when input evaluates to true`() {
        val config = EcgConfigFactory().create(fields, arrayOf("Ja", "", "", "", "", "", "", ""))
        assertThat(config.errors).isEmpty()
        assertThat(config.config).isEqualTo(ComorbidityConfig("Ja", ignore = false, curated = Ecg(null, null, null)))
    }

    @Test
    fun `Should return validation error when QTCF is not a number`() {
        val config = EcgConfigFactory().create(fields, arrayOf(INPUT, "interpretation", "1", "invalid", "ms", "1", "1", "ms"))
        assertThat(config.errors).containsExactly(
            CurationConfigValidationError(
                CurationCategory.ECG.categoryName,
                INPUT,
                "qtcfValue",
                "invalid",
                "integer"
            )
        )
    }

    @Test
    fun `Should return validation error when JTC is not a number`() {
        val config = EcgConfigFactory().create(fields, arrayOf(INPUT, "interpretation", "1", "1", "ms", "1", "invalid", "ms"))
        assertThat(config.errors).containsExactly(CurationConfigValidationError(
            CurationCategory.ECG.categoryName,
            INPUT,
            "jtcValue",
            "invalid",
            "integer"
        ))
    }
}