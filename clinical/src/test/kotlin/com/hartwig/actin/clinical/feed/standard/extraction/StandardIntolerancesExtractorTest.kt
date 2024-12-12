package com.hartwig.actin.clinical.feed.standard.extraction

import com.hartwig.actin.clinical.AtcModel
import com.hartwig.actin.clinical.curation.CurationDatabase
import com.hartwig.actin.clinical.curation.config.IntoleranceConfig
import com.hartwig.actin.clinical.feed.standard.EhrTestData
import com.hartwig.actin.clinical.feed.standard.ProvidedAllergy
import com.hartwig.actin.clinical.feed.standard.ProvidedPriorOtherCondition
import com.hartwig.actin.datamodel.clinical.IcdCode
import com.hartwig.actin.datamodel.clinical.Intolerance
import com.hartwig.actin.datamodel.clinical.treatment.TreatmentCategory
import io.mockk.every
import io.mockk.mockk
import org.assertj.core.api.Assertions.assertThat
import org.junit.Test
import java.time.LocalDate

private const val NAME = "allergy"
private const val CATEGORY = "category"
private const val CLINICAL_STATUS = "clinicalStatus"
private const val VERIFICATION_STATUS = "verificationStatus"
private const val SEVERITY = "severity"
private const val CURATED = "curated"
private const val DOID = "doid"
private const val ICD = "icd"
private const val SUBCATEGORY = "subcategory"

private val EHR_PATIENT_RECORD = EhrTestData.createEhrPatientRecord().copy(
    allergies = listOf(
        ProvidedAllergy(
            name = NAME,
            category = CATEGORY,
            clinicalStatus = CLINICAL_STATUS,
            verificationStatus = VERIFICATION_STATUS,
            severity = SEVERITY,
            startDate = LocalDate.of(2024, 4, 22),
            endDate = LocalDate.of(2024, 4, 22)
        )
    )
)

class StandardIntolerancesExtractorTest {

    private val atcModel = mockk<AtcModel>()
    private val intoleranceCuration = mockk<CurationDatabase<IntoleranceConfig>> {
        every { find(any()) } returns emptySet()
    }
    private val extractor = StandardIntolerancesExtractor(atcModel, intoleranceCuration)

    @Test
    fun `Should extract intolerances from allergies when no curation present`() {
        val results = extractor.extract(EHR_PATIENT_RECORD)
        assertThat(results.extracted).containsExactly(
            Intolerance(
                name = NAME,
                category = CATEGORY,
                clinicalStatus = CLINICAL_STATUS,
                verificationStatus = VERIFICATION_STATUS,
                criticality = SEVERITY,
                icdCode = IcdCode("", null),
                doids = emptySet(),
                subcategories = emptySet(),
                treatmentCategories = emptySet()
            )
        )
    }

    @Test
    fun `Should extract intolerances from allergies and augment with curation when present`() {
        every { atcModel.resolveByName(CURATED) } returns setOf(SUBCATEGORY)
        every { intoleranceCuration.find(NAME) } returns setOf(
            IntoleranceConfig(
                input = NAME,
                name = CURATED,
                icd = IcdCode(ICD, null),
                doids = setOf(DOID),
                treatmentCategories = setOf(TreatmentCategory.CHEMOTHERAPY)
            )
        )
        val results = extractor.extract(EHR_PATIENT_RECORD)
        assertThat(results.extracted).containsExactly(
            Intolerance(
                name = CURATED,
                icdCode = IcdCode(ICD, null),
                category = CATEGORY,
                clinicalStatus = CLINICAL_STATUS,
                verificationStatus = VERIFICATION_STATUS,
                criticality = SEVERITY,
                doids = setOf(DOID),
                subcategories = setOf(SUBCATEGORY),
                treatmentCategories = setOf(TreatmentCategory.CHEMOTHERAPY)
            )
        )
        assertThat(results.evaluation.warnings).isEmpty()
    }

    @Test
    fun `Should extract intolerances from prior other conditions, supporting multiple configs per input, but ignore any curation warnings`() {
        val anotherCurated = "another curated"
        every { atcModel.resolveByName(CURATED) } returns setOf(SUBCATEGORY)
        every { atcModel.resolveByName(anotherCurated) } returns emptySet()
        every { intoleranceCuration.find(NAME) } returns setOf(
            IntoleranceConfig(
                input = NAME,
                name = CURATED,
                icd = IcdCode(ICD, null),
                doids = setOf(DOID),
                treatmentCategories = setOf(TreatmentCategory.CHEMOTHERAPY)
            ),
            IntoleranceConfig(
                input = NAME,
                name = anotherCurated,
                icd = IcdCode(ICD, null),
                doids = emptySet(),
                treatmentCategories = emptySet()
            )
        )
        val results = extractor.extract(
            EHR_PATIENT_RECORD.copy(
                allergies = emptyList(),
                priorOtherConditions = listOf(
                    ProvidedPriorOtherCondition(name = NAME, startDate = LocalDate.of(2024, 4, 22)),
                    ProvidedPriorOtherCondition(name = "not an intolerance", startDate = LocalDate.of(2024, 4, 22))
                )
            )
        )
        assertThat(results.extracted).containsExactly(
            Intolerance(
                name = CURATED,
                icdCode = IcdCode(ICD, null),
                doids = setOf(DOID),
                subcategories = setOf(SUBCATEGORY),
                treatmentCategories = setOf(TreatmentCategory.CHEMOTHERAPY)
            ),
            Intolerance(
                name = anotherCurated,
                icdCode = IcdCode(ICD, null),
                doids = emptySet(),
                subcategories = emptySet(),
                treatmentCategories = emptySet()
            )
        )
        assertThat(results.evaluation.warnings).isEmpty()
    }

}