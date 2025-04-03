package com.hartwig.actin.clinical.feed.emc.vitalfunction

import org.assertj.core.api.Assertions.assertThat
import org.junit.Test
import java.time.LocalDateTime

class VitalFunctionFeedValidatorTest {

    private val validator = VitalFunctionFeedValidator()

    @Test
    fun `Should return no warnings when all fields are valid`() {
        val feed = validEntry()
        val result = validator.validate(feed)
        assertThat(result.valid).isTrue
        assertThat(result.warnings).isEmpty()
    }

    @Test
    fun `Should return warning when codeDisplayOriginal is empty`() {
        val feed = validEntry().copy(codeDisplayOriginal = "")

        val result = validator.validate(feed)

        assertThat(result.valid).isFalse
        assertThat(result.warnings).hasSize(1)
        assertThat(result.warnings[0].message).isEqualTo("Empty vital function category")
    }

    @Test
    fun `Should return warning when quantityValue is null`() {
        val feed = validEntry().copy(quantityValue = null)

        val result = validator.validate(feed)

        assertThat(result.valid).isFalse
        assertThat(result.warnings).hasSize(1)
        assertThat(result.warnings[0].message).isEqualTo("Empty vital function value")
    }

    @Test
    fun `Should return warning when codeDisplayOriginal is invalid`() {
        val feed = validEntry().copy(codeDisplayOriginal = "Invalid")

        val result = validator.validate(feed)

        assertThat(result.valid).isFalse
        assertThat(result.warnings).hasSize(1)
        assertThat(result.warnings[0].message).isEqualTo("Invalid vital function category: Invalid")
    }

    private fun validEntry() = VitalFunctionEntry(
        subject = "TestSubject",
        effectiveDateTime = LocalDateTime.now(),
        codeDisplayOriginal = "NIBP",
        componentCodeDisplay = "TestComponent",
        quantityUnit = "TestUnit",
        quantityValue = 1.0
    )
}