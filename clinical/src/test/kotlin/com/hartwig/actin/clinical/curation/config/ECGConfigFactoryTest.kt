package com.hartwig.actin.clinical.curation.config

import com.hartwig.actin.clinical.curation.CURATION_DIRECTORY
import com.hartwig.actin.clinical.curation.CurationDatabaseReader
import org.assertj.core.api.Assertions.assertThat
import org.junit.Test


class ECGConfigFactoryTest {
    private val fields: Map<String, Int> = CurationConfigFile.readTsv(CURATION_DIRECTORY + CurationDatabaseReader.ECG_TSV).second

    @Test
    fun `Should return ECGConfig from valid inputs`() {
        val config = ECGConfigFactory().create(fields, arrayOf("input", "interpretation", "1", "1", "ms", "1", "1", "ms"))
        assertThat(config.errors).isEmpty()
        assertThat(config.config.input).isEqualTo("input")
        assertThat(config.config.interpretation).isEqualTo("interpretation")
        assertThat(config.config.isQTCF).isEqualTo(true)
        assertThat(config.config.qtcfValue).isEqualTo(1)
        assertThat(config.config.qtcfUnit).isEqualTo("ms")
        assertThat(config.config.isJTC).isEqualTo(true)
        assertThat(config.config.jtcValue).isEqualTo(1)
        assertThat(config.config.jtcUnit).isEqualTo("ms")
    }

    @Test
    fun `Should return validation error when qtcf is not a number`() {
        val config: ValidatedCurationConfig<ECGConfig> =
            ECGConfigFactory().create(fields, arrayOf("input", "interpretation", "1", "string", "ms", "1", "1", "ms"))
        assertThat(config.errors).containsExactly(CurationConfigValidationError("The input 'string' for 'qtcf' is not a valid integer"))
    }

    @Test
    fun `Should return validation error when jtc is not a number`() {
        val config: ValidatedCurationConfig<ECGConfig> =
            ECGConfigFactory().create(fields, arrayOf("input", "interpretation", "1", "1", "ms", "1", "string", "ms"))
        assertThat(config.errors).containsExactly(CurationConfigValidationError("The input 'string' for 'jtc' is not a valid integer"))
    }
}