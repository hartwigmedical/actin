package com.hartwig.actin.clinical.curation.config

import com.hartwig.actin.clinical.curation.CurationCategory
import com.hartwig.actin.clinical.curation.CurationDatabaseReader
import com.hartwig.actin.clinical.curation.TestCurationFactory
import org.assertj.core.api.Assertions.assertThat
import org.junit.Test


class ECGConfigFactoryTest {
    private val fields: Map<String, Int> = TestCurationFactory.curationHeaders(CurationDatabaseReader.ECG_TSV)

    @Test
    fun `Should return ECGConfig from valid inputs`() {
        val config = EcgConfigFactory().create(fields, arrayOf("input", "interpretation", "1", "1", "ms", "0", "", ""))
        assertThat(config.errors).isEmpty()
        assertThat(config.config.input).isEqualTo("input")
        assertThat(config.config.interpretation).isEqualTo("interpretation")
        assertThat(config.config.isQTCF).isEqualTo(true)
        assertThat(config.config.qtcfValue).isEqualTo(1)
        assertThat(config.config.qtcfUnit).isEqualTo("ms")
        assertThat(config.config.isJTC).isEqualTo(false)
        assertThat(config.config.jtcValue).isNull()
        assertThat(config.config.jtcUnit).isNull()
    }

    @Test
    fun `Should return validation error when qtcf is not a number`() {
        val config: ValidatedCurationConfig<ECGConfig> =
            EcgConfigFactory().create(fields, arrayOf("input", "interpretation", "1", "invalid", "ms", "1", "1", "ms"))
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
        val config: ValidatedCurationConfig<ECGConfig> =
            EcgConfigFactory().create(fields, arrayOf("input", "interpretation", "1", "1", "ms", "1", "invalid", "ms"))
        assertThat(config.errors).containsExactly(CurationConfigValidationError(
            CurationCategory.ECG.categoryName,
            "input",
            "jtcValue",
            "invalid",
            "integer"
        ))
    }
}