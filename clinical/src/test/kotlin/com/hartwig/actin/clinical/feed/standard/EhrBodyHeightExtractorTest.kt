package com.hartwig.actin.clinical.feed.standard

import com.hartwig.actin.clinical.datamodel.BodyHeight
import org.assertj.core.api.Assertions
import org.junit.Test
import java.time.LocalDateTime

private val CATEGORY = EhrMeasurementCategory.BODY_HEIGHT.name
private val DATE = LocalDateTime.of(2024, 4, 24, 0, 0, 0)
private val SUB_CATEGORY = null
private const val VALUE = 200.0
private const val UNIT = "centimeters"

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
class EhrBodyHeightExtractorTest {

    private val extractor = EhrBodyHeightExtractor()

    @Test
    fun `Should extract body height from EHR`() {
        val results = extractor.extract(EHR_PATIENT_RECORD)
        Assertions.assertThat(results.extracted).containsExactly(
            BodyHeight(
                date = DATE,
                value = VALUE,
                unit = UNIT,
                valid = true
            )
        )
    }
}