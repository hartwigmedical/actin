package com.hartwig.actin.clinical.curation.config

import com.hartwig.actin.clinical.curation.CurationCategory
import com.hartwig.actin.clinical.curation.CurationDatabaseReader
import com.hartwig.actin.clinical.curation.TestCurationFactory
import com.hartwig.actin.datamodel.clinical.QTProlongatingRisk
import org.assertj.core.api.Assertions.assertThat
import org.junit.Test


class QTProlongatingConfigFactoryTest {

    private val fields: Map<String, Int> =
        TestCurationFactory.curationHeaders(CurationDatabaseReader.QT_PROLONGATING_TSV)

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
                CurationCategory.QT_PROLONGATING.categoryName,
                "name",
                "Risk",
                "not known",
                "QTProlongatingRisk",
                "Accepted values are [KNOWN, POSSIBLE, CONDITIONAL, NONE, UNKNOWN]"
            )
        )
    }
}