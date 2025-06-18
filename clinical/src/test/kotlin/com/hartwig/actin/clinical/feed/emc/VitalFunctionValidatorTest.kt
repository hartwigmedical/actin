package com.hartwig.actin.clinical.feed.emc

import com.hartwig.feed.datamodel.FeedMeasurement
import org.assertj.core.api.Assertions.assertThat
import org.junit.Test
import java.time.LocalDateTime

class VitalFunctionValidatorTest {

    private val feedMeasurement = FeedMeasurement(
        date = LocalDateTime.now(), category = "NIBP", subcategory = "TestComponent", unit = "TestUnit", value = 1.0
    )

    private val patientId = "subject"

    private val validator = VitalFunctionValidator()

    @Test
    fun `Should return no warnings when all fields are valid`() {
        val result = validator.validate(patientId, feedMeasurement)
        assertThat(result.valid).isTrue
        assertThat(result.warnings).isEmpty()
    }

    @Test
    fun `Should return warning when codeDisplayOriginal is empty`() {
        val result = validator.validate(patientId, feedMeasurement.copy(category = ""))
        assertThat(result.valid).isFalse
        assertThat(result.warnings).hasSize(1)
        assertThat(result.warnings[0].message).isEqualTo("Empty vital function category")
    }

    @Test
    fun `Should return warning when quantityValue is missing`() {
        val result = validator.validate(patientId, feedMeasurement.copy(value = Double.NaN))
        assertThat(result.valid).isFalse
        assertThat(result.warnings).hasSize(1)
        assertThat(result.warnings[0].message).isEqualTo("Empty vital function value")
    }

    @Test
    fun `Should return warning when codeDisplayOriginal is invalid`() {
        val result = validator.validate(patientId, feedMeasurement.copy(category = "Invalid"))
        assertThat(result.valid).isFalse
        assertThat(result.warnings).hasSize(1)
        assertThat(result.warnings[0].message).isEqualTo("Invalid vital function category: Invalid")
    }
}