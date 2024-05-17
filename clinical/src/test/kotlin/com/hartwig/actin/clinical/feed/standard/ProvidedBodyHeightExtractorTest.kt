package com.hartwig.actin.clinical.feed.standard

import com.hartwig.actin.clinical.datamodel.BodyHeight
import java.time.LocalDateTime
import org.assertj.core.api.Assertions
import org.assertj.core.api.Assertions.assertThat
import org.junit.Test

private val CATEGORY = ProvidedMeasurementCategory.BODY_HEIGHT.name
private val DATE = LocalDateTime.of(2024, 4, 24, 0, 0, 0)
private val SUB_CATEGORY = null
private const val VALUE = 200.0
private const val UNIT = "centimeters"

private val EHR_PATIENT_RECORD = EhrTestData.createEhrPatientRecord().copy(
    measurements = listOf(
        ProvidedMeasurement(
            date = DATE.toLocalDate(),
            category = CATEGORY,
            subcategory = SUB_CATEGORY,
            value = VALUE,
            unit = UNIT
        )
    )
)
class ProvidedBodyHeightExtractorTest {

    private val extractor = ProvidedBodyHeightExtractor()

    @Test
    fun `Should extract body height from EHR`() {
        val results = extractor.extract(EHR_PATIENT_RECORD)
        assertThat(results.extracted).containsExactly(
            BodyHeight(
                date = DATE,
                value = VALUE,
                unit = UNIT,
                valid = true
            )
        )
    }

    @Test
    fun `Should throw IllegalArgumentException when the unit is not centimeters`() {
        val record = EHR_PATIENT_RECORD.copy(measurements = EHR_PATIENT_RECORD.measurements.map { it.copy(unit = "wrong") })
        Assertions.assertThatIllegalArgumentException()
            .isThrownBy { extractor.extract(record) }
            .withMessage("Unit of body height is not centimeters")
    }

    @Test
    fun `Should set valid property to false if value is outside of allowed range`() {
        val record = EHR_PATIENT_RECORD.copy(measurements = EHR_PATIENT_RECORD.measurements.map { it.copy(value = 300.0) })
        val results = extractor.extract(record)
        assertThat(results.extracted).containsExactly(
            BodyHeight(
                date = DATE,
                value = 300.0,
                unit = UNIT,
                valid = false
            )
        )
    }
}