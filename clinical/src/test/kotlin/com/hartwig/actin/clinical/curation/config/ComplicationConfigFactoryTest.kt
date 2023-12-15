package com.hartwig.actin.clinical.curation.config

import com.hartwig.actin.clinical.curation.CurationDatabaseReader
import com.hartwig.actin.clinical.curation.TestCurationFactory
import org.assertj.core.api.Assertions.assertThat
import org.junit.Test

class ComplicationConfigFactoryTest {

    private val fields: Map<String, Int> = TestCurationFactory.curationHeaders(CurationDatabaseReader.COMPLICATION_TSV)

    @Test
    fun `Should return complication config from valid data`() {
        val configFactory = ComplicationConfigFactory()
        val data = arrayOf("input", "1", "name", "categories", "2023", "12")
        val config = configFactory.create(fields, data)

        val errors = config.errors
        val configObj = config.config
        val curated = configObj.curated
        val curatedCategories = curated!!.categories()

        assertThat(errors).isEmpty()

        assertThat(configObj.input).isEqualTo("input")
        assertThat(configObj.ignore).isEqualTo(false)
        assertThat(configObj.impliesUnknownComplicationState).isTrue()

        assertThat(curated.name()).isEqualTo("name")
        assertThat(curatedCategories).containsExactly("categories")
        assertThat(curated.year()).isEqualTo(2023)
        assertThat(curated.month()).isEqualTo(12)
    }

    @Test
    fun `Should return validation error when year is not a number`() {
        assertThat(
            ComplicationConfigFactory().create(
                fields,
                arrayOf("input", "1", "name", "categories", "year", "12")
            ).errors
        ).containsExactly(
            CurationConfigValidationError("'year' had invalid value of 'year' for input 'input'")
        )
    }

    @Test
    fun `Should return validation error when month is not a number`() {
        assertThat(
            ComplicationConfigFactory().create(
                fields,
                arrayOf("input", "1", "name", "categories", "2023", "month")
            ).errors
        ).containsExactly(
            CurationConfigValidationError("'month' had invalid value of 'month' for input 'input'")
        )
    }

    @Test
    fun `Should return validation error when impliesUnknownComplicationState is not boolean`() {
        assertThat(
            ComplicationConfigFactory().create(
                fields,
                arrayOf("input", "A", "name", "categories", "2023", "12")
            ).errors
        ).containsExactly(
            CurationConfigValidationError("'impliesUnknownComplicationState' had invalid value of 'A' for input 'input'")
        )
    }
}