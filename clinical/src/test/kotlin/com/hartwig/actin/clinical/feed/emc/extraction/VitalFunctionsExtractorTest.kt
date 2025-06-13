package com.hartwig.actin.clinical.feed.emc.extraction

import com.hartwig.actin.clinical.curation.extraction.CurationExtractionEvaluation
import com.hartwig.actin.clinical.feed.standard.FeedTestData.FEED_PATIENT_RECORD
import com.hartwig.actin.datamodel.clinical.VitalFunctionCategory
import com.hartwig.feed.datamodel.FeedMeasurement
import org.assertj.core.api.Assertions.assertThat
import org.junit.Test
import java.time.LocalDateTime

class VitalFunctionsExtractorTest {

    private val extractor = VitalFunctionsExtractor()
    private val feedRecord = FEED_PATIENT_RECORD

    private val measurement = FeedMeasurement(
        date = LocalDateTime.now(),
        category = "NIBP",
        subcategory = "Systolic blood pressure",
        value = 108.0,
        unit = "mmHg"
    )

    @Test
    fun `Should extract empty list when measurements is empty`() {
        val (extracted, evaluation) = extractor.extract(feedRecord)
        assertThat(extracted).isEmpty()
        assertThat(evaluation).isEqualTo(CurationExtractionEvaluation())
    }

    @Test
    fun `Should extract empty list when measurements list has no vital measurements`() {
        val (extracted, evaluation) = extractor.extract(
            feedRecord.copy(measurements = listOf(measurement.copy(category = "BODY_WEIGHT")))
        )
        assertThat(extracted).isEmpty()
        assertThat(evaluation).isEqualTo(CurationExtractionEvaluation())
    }

    @Test(expected = IllegalArgumentException::class)
    fun `Should throw exception with invalid category`() {
        extractor.extract(feedRecord.copy(measurements = listOf(measurement.copy(category = "none"))))
    }

    @Test
    fun `Should extract valid blood pressure vital excluding measurements with value zero`() {
        val (extracted, evaluation) = extractor.extract(
            feedRecord.copy(
                measurements = listOf(measurement, measurement.copy(value = 0.0))
            )
        )
        assertThat(extracted).hasSize(1)
        assertThat(extracted[0].value).isEqualTo(108.0)
        assertThat(extracted[0].category).isEqualTo(VitalFunctionCategory.NON_INVASIVE_BLOOD_PRESSURE)
        assertThat(extracted[0].subcategory).isEqualTo("Systolic blood pressure")
        assertThat(extracted[0].unit).isEqualTo("mmHg")
        assertThat(extracted[0].valid).isTrue
        assertThat(evaluation).isEqualTo(CurationExtractionEvaluation())
    }

    @Test
    fun `Should extract blood pressure vital with invalid value`() {
        val (extracted, evaluation) = extractor.extract(feedRecord.copy(measurements = listOf(measurement.copy(value = 5.5))))
        assertThat(extracted).hasSize(1)
        assertThat(extracted[0].value).isEqualTo(5.5)
        assertThat(extracted[0].category).isEqualTo(VitalFunctionCategory.NON_INVASIVE_BLOOD_PRESSURE)
        assertThat(extracted[0].subcategory).isEqualTo("Systolic blood pressure")
        assertThat(extracted[0].unit).isEqualTo("mmHg")
        assertThat(extracted[0].valid).isFalse
        assertThat(evaluation).isEqualTo(CurationExtractionEvaluation())
    }

}