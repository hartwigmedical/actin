package com.hartwig.actin.clinical.curation.config

import org.assertj.core.api.Assertions.assertThat
import org.junit.Test

class ComplicationConfigFactoryTest {

    private val FIELDS = mapOf(
        "input" to 0,
        "name" to 1,
        "categories" to 2,
        "year" to 3,
        "month" to 4,
        "impliesUnknownComplicationState" to 5,
    )

    @Test
    fun `Should return validation error when impliesUnknownComplicationState is not boolean`() {
        assertThat(
            ComplicationConfigFactory().create(
                FIELDS,
                arrayOf("name", "input", "categories", "year", "month", "A")
            ).errors
        ).containsExactly(
            CurationConfigValidationError("")
        )
    }

}