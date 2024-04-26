package com.hartwig.actin.clinical.feed.standard

import com.hartwig.actin.clinical.datamodel.BodyWeight
import org.assertj.core.api.Assertions
import org.junit.Test
import java.time.LocalDateTime

private val CATEGORY = EhrMeasurementCategory.BODY_WEIGHT.name
private val DATE = LocalDateTime.of(2024, 4, 24, 0, 0, 0)
private val SUB_CATEGORY = null
private const val VALUE = 80.0
private const val UNIT = "Kilograms"

private val EHR_PATIENT_RECORD = EhrTestData.createEhrPatientRecord().copy(
    measurements = listOf(
        EhrMeasurement(
            date = DATE.toLocalDate(),
            category = CATEGORY,
            subcategory = SUB_CATEGORY,
            value = VALUE,
            unit = UNIT
        )
    )
)

class EhrBodyWeightExtractorTest {

    private val extractor = EhrBodyWeightExtractor()

    @Test
    fun `Should extract body weight from EHR`() {
        val results = extractor.extract(EHR_PATIENT_RECORD)
        Assertions.assertThat(results.extracted).containsExactly(
            BodyWeight(
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
            .withMessage("Unit of body weight is not Kilograms")
    }

    @Test
    fun `Should set valid property to false if value is outside of allowed range`() {
        val record = EHR_PATIENT_RECORD.copy(measurements = EHR_PATIENT_RECORD.measurements.map { it.copy(value = 300.0) })
        val results = extractor.extract(record)
        Assertions.assertThat(results.extracted).containsExactly(
            BodyWeight(
                date = DATE,
                value = 300.0,
                unit = UNIT,
                valid = false
            )
        )
    }
}
