package com.hartwig.actin.clinical.feed.standard.extraction

import com.hartwig.actin.clinical.curation.CurationCategory
import com.hartwig.actin.clinical.curation.CurationDatabase
import com.hartwig.actin.clinical.curation.CurationWarning
import com.hartwig.actin.clinical.curation.config.LabMeasurementConfig
import com.hartwig.actin.clinical.feed.standard.EhrTestData
import com.hartwig.actin.clinical.feed.standard.HASHED_ID_IN_BASE64
import com.hartwig.actin.clinical.feed.standard.ProvidedLabValue
import com.hartwig.actin.datamodel.clinical.LabMeasurement
import com.hartwig.actin.datamodel.clinical.LabUnit
import com.hartwig.actin.datamodel.clinical.LabValue
import io.mockk.every
import io.mockk.mockk
import org.assertj.core.api.Assertions.assertThat
import org.junit.Test
import java.time.LocalDate
import java.time.LocalDateTime

private const val LAB_CODE = "HGB"
private const val LAB_NAME = "Hemoglobine"

private val EHR_LAB_VALUE = ProvidedLabValue(
    evaluationTime = LocalDateTime.of(2024, 2, 28, 0, 0),
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
    labMeasurement = LabMeasurement.HEMOGLOBIN
)

private val LAB_VALUE = LabValue(
    date = LocalDate.of(2024, 2, 28),
    measurement = LabMeasurement.HEMOGLOBIN,
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
    fun `Should extract and curate lab values with known units`() {
        every { labCuration.find("$LAB_CODE | $LAB_NAME") } returns setOf(LAB_CONFIG)
        val result = extractor.extract(
            EhrTestData.createEhrPatientRecord().copy(
                labValues = listOf(
                    EHR_LAB_VALUE
                )
            )
        )
        assertThat(result.extracted).containsExactly(
            LAB_VALUE
        )
        assertThat(result.evaluation.warnings).isEmpty()
    }

    @Test
    fun `Should default unit to NONE when null`() {
        every { labCuration.find("$LAB_CODE | $LAB_NAME") } returns setOf(LAB_CONFIG)
        val result = extractor.extract(
            EhrTestData.createEhrPatientRecord().copy(
                labValues = listOf(
                    EHR_LAB_VALUE.copy(unit = null)
                )
            )
        )
        assertThat(result.extracted).containsExactly(
            LAB_VALUE.copy(unit = LabUnit.NONE)
        )
        assertThat(result.evaluation.warnings).isEmpty()
    }

    @Test
    fun `Should return curation warning when curation not found`() {
        every { labCuration.find("$LAB_CODE | $LAB_NAME") } returns emptySet()
        val result = extractor.extract(
            EhrTestData.createEhrPatientRecord().copy(
                labValues = listOf(
                    EHR_LAB_VALUE
                )
            )
        )
        assertThat(result.evaluation.warnings).containsExactly(
            CurationWarning(
                patientId = HASHED_ID_IN_BASE64,
                category = CurationCategory.LABORATORY,
                feedInput = "$LAB_CODE | $LAB_NAME",
                message = "Could not find laboratory config for input '$LAB_CODE | $LAB_NAME'"
            )
        )
    }
}