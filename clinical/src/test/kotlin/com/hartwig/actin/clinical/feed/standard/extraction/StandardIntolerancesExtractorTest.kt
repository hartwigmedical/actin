package com.hartwig.actin.clinical.feed.standard.extraction

import com.hartwig.actin.clinical.curation.CurationDatabase
import com.hartwig.actin.clinical.curation.config.ComorbidityConfig
import com.hartwig.actin.clinical.feed.standard.EhrTestData
import com.hartwig.actin.clinical.feed.standard.ProvidedAllergy
import com.hartwig.actin.datamodel.clinical.IcdCode
import com.hartwig.actin.datamodel.clinical.Intolerance
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
private const val ICD = "icd"

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

    private val intoleranceCuration = mockk<CurationDatabase<ComorbidityConfig>> {
        every { find(any()) } returns emptySet()
    }
    private val extractor = StandardIntolerancesExtractor(intoleranceCuration)

    @Test
    fun `Should extract intolerances from allergies when no curation present`() {
        val results = extractor.extract(EHR_PATIENT_RECORD)
        assertThat(results.extracted).containsExactly(
            Intolerance(
                name = NAME,
                clinicalStatus = CLINICAL_STATUS,
                verificationStatus = VERIFICATION_STATUS,
                criticality = SEVERITY,
                icdCodes = setOf(IcdCode("", null)),
            )
        )
    }

    @Test
    fun `Should extract intolerances from allergies and augment with curation when present`() {
        every { intoleranceCuration.find(NAME) } returns setOf(
            ComorbidityConfig(
                input = NAME,
                ignore = false,
                curated = Intolerance(
                    name = CURATED,
                    icdCodes = setOf(IcdCode(ICD, null))
                )
            )
        )
        val results = extractor.extract(EHR_PATIENT_RECORD)
        assertThat(results.extracted).containsExactly(
            Intolerance(
                name = CURATED,
                icdCodes = setOf(IcdCode(ICD, null)),
                clinicalStatus = CLINICAL_STATUS,
                verificationStatus = VERIFICATION_STATUS,
                criticality = SEVERITY,
            )
        )
        assertThat(results.evaluation.warnings).isEmpty()
    }
}