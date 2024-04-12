package com.hartwig.actin.clinical.feed.standard

import com.hartwig.actin.clinical.curation.CurationCategory
import com.hartwig.actin.clinical.curation.CurationWarning
import com.hartwig.actin.clinical.curation.translation.LaboratoryIdentifiers
import com.hartwig.actin.clinical.curation.translation.Translation
import com.hartwig.actin.clinical.curation.translation.TranslationDatabase
import com.hartwig.actin.clinical.datamodel.LabUnit
import com.hartwig.actin.clinical.datamodel.LabValue
import io.mockk.every
import io.mockk.mockk
import java.time.LocalDate
import java.time.LocalDateTime
import org.assertj.core.api.Assertions.assertThat
import org.junit.Test

private const val LAB_CODE = "HGB"
private const val LAB_NAME = "Hemoglobie"
private val LAB_IDENTIFIERS = LaboratoryIdentifiers(LAB_CODE, LAB_NAME)
private const val HEMOGLOBIN_TRANSLATED = "Hemoglobin"

private val EHR_LAB_VALUE = EhrLabValue(
    evaluationTime = LocalDateTime.of(2024, 2, 28, 0, 0),
    measure = LAB_NAME,
    measureCode = LAB_CODE,
    value = 12.0,
    unit = "g/dL",
    comparator = ">",
    refLowerBound = 12.0,
    refUpperBound = 16.0,
    refFlag = "N"
)

private val LAB_VALUE = LabValue(
    date = LocalDate.of(2024, 2, 28),
    name = HEMOGLOBIN_TRANSLATED,
    unit = LabUnit.GRAMS_PER_DECILITER,
    value = 12.0,
    code = LAB_CODE,
    comparator = ">",
    refLimitUp = 16.0,
    refLimitLow = 12.0
)

class EhrLabValuesExtractorTest {

    private val labTranslationDatabase = mockk<TranslationDatabase<LaboratoryIdentifiers>>()
    private val extractor = EhrLabValuesExtractor(labTranslationDatabase)

    @Test
    fun `Should extract and translate lab values with known units`() {
        setupTranslation()
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
        setupTranslation()
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
    fun `Should return curation warning when translation not found`() {
        every { labTranslationDatabase.find(LAB_IDENTIFIERS) } returns null
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
                category = CurationCategory.LABORATORY_TRANSLATION,
                feedInput = "$LAB_CODE | $LAB_NAME",
                message = "Could not find laboratory translation for lab value with code '$LAB_CODE' and name '$LAB_NAME'"
            )
        )
    }

    private fun setupTranslation() {
        every {
            labTranslationDatabase.find(
                LAB_IDENTIFIERS
            )
        } returns Translation(LAB_IDENTIFIERS, LaboratoryIdentifiers(LAB_CODE, HEMOGLOBIN_TRANSLATED))
    }

}