package com.hartwig.actin.clinical.feed.standard.extraction

import com.hartwig.actin.clinical.curation.extraction.CurationExtractionEvaluation
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
private const val UNIT = "kilograms"

private val measurement = FeedMeasurement(
    date = DATE,
    category = CATEGORY,
    subcategory = SUB_CATEGORY,
    value = VALUE,
    unit = UNIT
)

private val FEED_PATIENT_RECORD = FeedTestData.FEED_PATIENT_RECORD

class StandardBodyWeightExtractorTest {

    private val extractor = StandardBodyWeightExtractor()

    @Test
    fun `Should extract empty list when measurements is empty`() {
        val (extracted, evaluation) = extractor.extract(FEED_PATIENT_RECORD)
        assertThat(extracted).isEmpty()
        assertThat(evaluation).isEqualTo(CurationExtractionEvaluation())
    }

    @Test
    fun `Should extract empty list when measurements list has no weight measurements`() {
        val (extracted, evaluation) = extractor.extract(
            FEED_PATIENT_RECORD.copy(
                measurements = listOf(measurement.copy(category = "none"))
            )
        )
        assertThat(extracted).isEmpty()
        assertThat(evaluation).isEqualTo(CurationExtractionEvaluation())
    }

    @Test
    fun `Should extract body weight from EHR`() {
        val results = extractor.extract(FEED_PATIENT_RECORD.copy(measurements = listOf(measurement)))
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
    fun `Should extract body weight excluding measurements with value zero`() {
        val (extracted, evaluation) = extractor.extract(
            FEED_PATIENT_RECORD.copy(
                measurements = listOf(measurement, measurement.copy(value = 0.0))
            )
        )
        assertThat(extracted).hasSize(1)
        assertThat(extracted[0].value).isEqualTo(VALUE)
        assertThat(extracted[0].unit).isEqualTo(UNIT)
        assertThat(extracted[0].valid).isTrue
        assertThat(evaluation).isEqualTo(CurationExtractionEvaluation())
    }

    @Test
    fun `Should throw IllegalArgumentException when the unit is not Kilograms`() {
        val record = FEED_PATIENT_RECORD.copy(measurements = listOf(measurement.copy(unit = "wrong")))
        assertThatIllegalArgumentException()
            .isThrownBy { extractor.extract(record) }
            .withMessage("Unit of body weight is not Kilograms")
    }

    @Test
    fun `Should set valid property to false if value is outside of allowed range`() {
        val results = extractor.extract(
            FEED_PATIENT_RECORD.copy(
                measurements = listOf(measurement.copy(value = 310.0))
            )
        )
        assertThat(results.extracted).containsExactly(
            BodyWeight(
                date = DATE,
                value = 310.0,
                unit = UNIT,
                valid = false
            )
        )
    }
}
