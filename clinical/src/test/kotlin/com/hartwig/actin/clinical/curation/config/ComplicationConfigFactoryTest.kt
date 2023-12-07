package com.hartwig.actin.clinical.curation.config

import org.assertj.core.api.Assertions.assertThat
import org.junit.Test

class ComplicationConfigFactoryTest {

    private val fields: Map<String, Int> = mapOf(
        "input" to 0,
        "name" to 1,
        "categories" to 2,
        "year" to 3,
        "month" to 4,
        "impliesUnknownComplicationState" to 5,
    )

    @Test
    fun `Should return complication config from valid data`() {
        val configFactory = ComplicationConfigFactory()
        val data = arrayOf("input", "name", "categories", "2023", "12", "1")
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
                arrayOf("input", "name", "categories", "year", "12", "1")
            ).errors
        ).containsExactly(
            CurationConfigValidationError("'year' had invalid input of 'year'")
        )
    }

    @Test
    fun `Should return validation error when month is not a number`() {
        assertThat(
            ComplicationConfigFactory().create(
                fields,
                arrayOf("input", "name", "categories", "2023", "month", "1")
            ).errors
        ).containsExactly(
            CurationConfigValidationError("'month' had invalid input of 'month'")
        )
    }

    @Test
    fun `Should return validation error when impliesUnknownComplicationState is not boolean`() {
        assertThat(
            ComplicationConfigFactory().create(
                fields,
                arrayOf("name", "input", "categories", "2023", "12", "A")
            ).errors
        ).containsExactly(
            CurationConfigValidationError("impliesComplicationState had invalid input of 'A'")
        )
    }

}