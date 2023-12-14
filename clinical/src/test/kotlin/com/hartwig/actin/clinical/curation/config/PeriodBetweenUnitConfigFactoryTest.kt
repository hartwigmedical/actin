package com.hartwig.actin.clinical.curation.config

import com.hartwig.actin.clinical.curation.CurationDatabaseReader
import com.hartwig.actin.clinical.curation.TestCurationFactory
import org.assertj.core.api.Assertions.assertThat
import org.junit.Test

class PeriodBetweenUnitConfigFactoryTest {

    private val fields: Map<String, Int> =
        TestCurationFactory.curationHeaders(CurationDatabaseReader.PERIOD_BETWEEN_UNIT_TSV)

    @Test
    fun `Should return PeriodBetweenUnitConfig from valid inputs`() {
        val config = PeriodBetweenUnitConfigFactory().create(fields, arrayOf("input", "interpretation"))
        assertThat(config.errors).isEmpty()
        assertThat(config.config.input).isEqualTo("input")
        assertThat(config.config.interpretation).isEqualTo("interpretation")
    }
}