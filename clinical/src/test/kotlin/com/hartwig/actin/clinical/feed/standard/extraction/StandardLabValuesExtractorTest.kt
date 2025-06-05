package com.hartwig.actin.clinical.feed.standard.extraction

import com.hartwig.actin.datamodel.clinical.ingestion.CurationCategory
import com.hartwig.actin.datamodel.clinical.ingestion.CurationWarning
import com.hartwig.actin.clinical.curation.CurationDatabase
import com.hartwig.actin.clinical.curation.config.LabMeasurementConfig
import com.hartwig.actin.clinical.feed.standard.FeedTestData
import com.hartwig.actin.clinical.feed.standard.FeedTestData.FEED_PATIENT_RECORD
import com.hartwig.actin.clinical.feed.standard.HASHED_ID_IN_BASE64
import com.hartwig.actin.datamodel.clinical.LabMeasurement
import com.hartwig.actin.datamodel.clinical.LabUnit
import com.hartwig.actin.datamodel.clinical.LabValue
import com.hartwig.feed.datamodel.FeedLabValue
import io.mockk.every
import io.mockk.mockk
import org.assertj.core.api.Assertions.assertThat
import org.junit.Test
import java.time.LocalDate

private const val LAB_CODE = "Hb"
private const val LAB_NAME = "Hemoglobine"
private val LAB_MEASUREMENT = LabMeasurement.HEMOGLOBIN

private val EHR_LAB_VALUE = FeedLabValue(
    date = LocalDate.of(2024, 2, 28),
    measure = LAB_NAME,
    measureCode = LAB_CODE,
    value = 12.0,
    unit = "g/dL",
    comparator = ">",
    refLowerBound = 12.0,
    refUpperBound = 16.0
)

private val LAB_CONFIG = LabMeasurementConfig(
    input = "$LAB_CODE | $LAB_NAME",
    ignore = false,
    labMeasurement = LAB_MEASUREMENT
)

private val LAB_VALUE = LabValue(
    date = LocalDate.of(2024, 2, 28),
    measurement = LAB_MEASUREMENT,
    unit = LabUnit.GRAMS_PER_DECILITER,
    value = 12.0,
    comparator = ">",
    refLimitUp = 16.0,
    refLimitLow = 12.0
)

class StandardLabValuesExtractorTest {

    private val labCuration = mockk<CurationDatabase<LabMeasurementConfig>>()
    private val extractor = StandardLabValuesExtractor(labCuration)

    @Test
    fun `Should extract and curate lab values with lab measurement and known units`() {
        every { labCuration.find("$LAB_CODE | $LAB_NAME") } returns setOf(LAB_CONFIG)
        val result = extractor.extract(
            FEED_PATIENT_RECORD.copy(labValues = listOf(EHR_LAB_VALUE))
        )
        assertThat(result.extracted).containsExactly(LAB_VALUE)
        assertThat(result.evaluation.warnings).isEmpty()
    }

    @Test
    fun `Should default unit to NONE when null`() {
        every { labCuration.find("$LAB_CODE | $LAB_NAME") } returns setOf(LAB_CONFIG)
        val result = extractor.extract(
            FEED_PATIENT_RECORD.copy(labValues = listOf(EHR_LAB_VALUE.copy(unit = null)))
        )
        assertThat(result.extracted).containsExactly(LAB_VALUE.copy(unit = LabUnit.NONE))
        assertThat(result.evaluation.warnings).isEmpty()
    }

    @Test
    fun `Should return curation warning when curation not found`() {
        every { labCuration.find("$LAB_CODE | $LAB_NAME") } returns emptySet()
        val result = extractor.extract(
            FEED_PATIENT_RECORD.copy(labValues = listOf(EHR_LAB_VALUE))
        )
        assertThat(result.evaluation.warnings).containsExactly(
            CurationWarning(
                patientId = HASHED_ID_IN_BASE64,
                category = CurationCategory.LAB_MEASUREMENT,
                feedInput = "$LAB_CODE | $LAB_NAME",
                message = "Could not find lab measurement config for input '$LAB_CODE | $LAB_NAME'"
            )
        )
    }
}