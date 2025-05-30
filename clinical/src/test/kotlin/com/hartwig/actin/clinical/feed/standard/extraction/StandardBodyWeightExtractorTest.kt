package com.hartwig.actin.clinical.feed.standard.extraction

import com.hartwig.actin.clinical.feed.standard.FeedTestData
import com.hartwig.actin.datamodel.clinical.BodyWeight
import com.hartwig.feed.datamodel.FeedMeasurement
import org.assertj.core.api.Assertions.assertThat
import org.assertj.core.api.Assertions.assertThatIllegalArgumentException
import org.junit.Test
import java.time.LocalDateTime

private val CATEGORY = MeasurementCategory.BODY_WEIGHT.name
private val DATE = LocalDateTime.of(2024, 4, 24, 0, 0, 0)
private val SUB_CATEGORY = null
private const val VALUE = 80.0
private const val UNIT = "Kilograms"

private val FEED_PATIENT_RECORD = FeedTestData.FEED_PATIENT_RECORD.copy(
    measurements = listOf(
        FeedMeasurement(
            date = DATE.toLocalDate(),
            category = CATEGORY,
            subcategory = SUB_CATEGORY,
            value = VALUE,
            unit = UNIT
        )
    )
)

class StandardBodyWeightExtractorTest {

    private val extractor = StandardBodyWeightExtractor()

    @Test
    fun `Should extract body weight from EHR`() {
        val results = extractor.extract(FEED_PATIENT_RECORD)
        assertThat(results.extracted).containsExactly(
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
        val record = FEED_PATIENT_RECORD.copy(measurements = FEED_PATIENT_RECORD.measurements.map { it.copy(unit = "wrong") })
        assertThatIllegalArgumentException()
            .isThrownBy { extractor.extract(record) }
            .withMessage("Unit of body weight is not Kilograms")
    }

    @Test
    fun `Should set valid property to false if value is outside of allowed range`() {
        val record = FEED_PATIENT_RECORD.copy(measurements = FEED_PATIENT_RECORD.measurements.map { it.copy(value = 300.0) })
        val results = extractor.extract(record)
        assertThat(results.extracted).containsExactly(
            BodyWeight(
                date = DATE,
                value = 300.0,
                unit = UNIT,
                valid = false
            )
        )
    }
}
