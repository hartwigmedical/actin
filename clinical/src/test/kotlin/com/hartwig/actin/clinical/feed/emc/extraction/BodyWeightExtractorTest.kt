package com.hartwig.actin.clinical.feed.emc.extraction

import com.hartwig.actin.clinical.curation.extraction.CurationExtractionEvaluation
import com.hartwig.actin.clinical.feed.standard.FeedTestData.FEED_PATIENT_RECORD
import com.hartwig.feed.datamodel.FeedMeasurement
import org.assertj.core.api.Assertions.assertThat
import org.junit.Test
import java.time.LocalDateTime

class BodyWeightExtractorTest {

    private val extractor = BodyWeightExtractor()
    private val feedRecord = FEED_PATIENT_RECORD

    private val measurement = FeedMeasurement(
        date = LocalDateTime.now(),
        category = "BODY_WEIGHT",
        subcategory = "subcategory",
        value = 50.5,
        unit = "kilogram"
    )

    @Test
    fun `Should extract empty list when measurements is empty`() {
        val (extracted, evaluation) = extractor.extract(feedRecord)
        assertThat(extracted).isEmpty()
        assertThat(evaluation).isEqualTo(CurationExtractionEvaluation())
    }

    @Test
    fun `Should extract empty list when measurements list has no weight measurements`() {
        val (extracted, evaluation) = extractor.extract(
            feedRecord.copy(
                measurements = listOf(measurement.copy(category = "none"))
            )
        )
        assertThat(extracted).isEmpty()
        assertThat(evaluation).isEqualTo(CurationExtractionEvaluation())
    }


    @Test
    fun `Should extract body weight excluding measurements with value zero`() {
        val (extracted, evaluation) = extractor.extract(
            feedRecord.copy(
                measurements = listOf(measurement, measurement.copy(value = 0.0))
            )
        )
        assertThat(extracted).hasSize(1)
        assertThat(extracted[0].value).isEqualTo(50.5)
        assertThat(extracted[0].unit).isEqualTo("kilogram")
        assertThat(extracted[0].valid).isTrue
        assertThat(evaluation).isEqualTo(CurationExtractionEvaluation())
    }

    @Test
    fun `Should extract body weight with invalid value`() {
        val (extracted, evaluation) = extractor.extract(feedRecord.copy(measurements = listOf(measurement.copy(value = 10.5))))
        assertThat(extracted).hasSize(1)
        assertThat(extracted[0].value).isEqualTo(10.5)
        assertThat(extracted[0].unit).isEqualTo("kilogram")
        assertThat(extracted[0].valid).isFalse
        assertThat(evaluation).isEqualTo(CurationExtractionEvaluation())
    }
}