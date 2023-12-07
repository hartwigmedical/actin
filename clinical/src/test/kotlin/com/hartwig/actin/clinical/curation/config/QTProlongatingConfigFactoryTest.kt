package com.hartwig.actin.clinical.curation.config

import com.hartwig.actin.clinical.curation.CURATION_DIRECTORY
import com.hartwig.actin.clinical.curation.CurationDatabaseReader
import com.hartwig.actin.clinical.datamodel.QTProlongatingRisk
import org.assertj.core.api.Assertions.assertThat
import org.junit.Test


class QTProlongatingConfigFactoryTest {

    private val fields: Map<String, Int> =
        CurationConfigFile.readTsv(CURATION_DIRECTORY + CurationDatabaseReader.QT_PROLONGATING_TSV).second

    @Test
    fun `Should return QTProlongatingConfig from valid inputs`() {
        val config = QTProlongatingConfigFactory().create(fields, arrayOf("name", "KNOWN"))
        assertThat(config.errors).isEmpty()
        assertThat(config.config.input).isEqualTo("name")
        assertThat(config.config.ignore).isEqualTo(false)
        assertThat(config.config.status).isEqualTo(QTProlongatingRisk.KNOWN)
    }

    @Test
    fun `Should return QTProlongatingConfig from valid inputs when risk is lowercase and has whitespace`() {
        val config = QTProlongatingConfigFactory().create(fields, arrayOf("name", "known "))
        assertThat(config.errors).isEmpty()
        assertThat(config.config.input).isEqualTo("name")
        assertThat(config.config.ignore).isEqualTo(false)
        assertThat(config.config.status).isEqualTo(QTProlongatingRisk.KNOWN)
    }

    @Test
    fun `Should return validation error when invalid risk value`() {
        val config = QTProlongatingConfigFactory().create(fields, arrayOf("name", "not known"))
        assertThat(config.errors).containsExactly(
            CurationConfigValidationError(
                "Invalid enum value 'NOT KNOWN' for enum 'QTProlongatingRisk'. " +
                        "Accepted values are [KNOWN, POSSIBLE, CONDITIONAL, NONE, UNKNOWN]"
            )
        )
    }
}