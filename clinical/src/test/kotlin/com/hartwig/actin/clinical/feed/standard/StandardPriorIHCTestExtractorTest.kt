package com.hartwig.actin.clinical.feed.standard

import com.hartwig.actin.clinical.curation.CurationCategory
import com.hartwig.actin.clinical.curation.CurationDatabase
import com.hartwig.actin.clinical.curation.CurationWarning
import com.hartwig.actin.clinical.curation.config.MolecularTestConfig
import com.hartwig.actin.clinical.datamodel.PriorIHCTest
import com.hartwig.actin.clinical.feed.standard.EhrTestData.createEhrPatientRecord
import com.hartwig.actin.molecular.datamodel.TEST_DATE
import io.mockk.every
import io.mockk.mockk
import java.time.LocalDate
import org.assertj.core.api.Assertions.assertThat
import org.junit.Test

private const val IHC_LINE = "HER2 immunohistochemie: negative"
private val PRIOR_IHC_TEST =
    PriorIHCTest(item = "HER2", measure = "negative", impliesPotentialIndeterminateStatus = true)
private const val MICROSCOPIE_LINE = "TTF1 negatief"

private const val PATHOLOGY_REPORT =
    "Microscopie:\n$MICROSCOPIE_LINE\n\nConclusie:\n\nunrelated.\r\n\r\n\r\n$IHC_LINE\n\n"
private val PRIOR_MOLECULAR_TEST =
    PriorIHCTest(
        item = "HER2",
        measure = "Positive",
        measureDate = LocalDate.parse("2024-03-25"),
        scoreText = "variant",
        impliesPotentialIndeterminateStatus = false
    )
private val EHR_PATIENT_RECORD = createEhrPatientRecord()
private val EHR_PATIENT_RECORD_WITH_PATHOLOGY =
    EHR_PATIENT_RECORD.copy(tumorDetails = EHR_PATIENT_RECORD.tumorDetails.copy(tumorGradeDifferentiation = PATHOLOGY_REPORT))
private val EHR_PATIENT_RECORD_WITH_IHC_TEST =
    EHR_PATIENT_RECORD.copy(
        molecularTestHistory = listOf(
            ProvidedMolecularTest(
                "IHC", TEST_DATE, "PALGA", emptySet(), results = setOf(
                    ProvidedMolecularTestResult(gene = "HER2", ihcResult = "Negative")
                )
            )
        )
    )

private val UNUSED_DATE = LocalDate.of(2024, 4, 15)


class StandardPriorIHCTestExtractorTest {

    private val molecularTestCuration = mockk<CurationDatabase<MolecularTestConfig>> {
        every { find(any()) } returns emptySet()
    }
    private val extractor = StandardPriorIHCTestExtractor(molecularTestCuration)

    @Test
    fun `Should return no molecular test configs when tumor differentiation is null`() {
        val noDifferentiation =
            EHR_PATIENT_RECORD.copy(tumorDetails = EHR_PATIENT_RECORD.tumorDetails.copy(tumorGradeDifferentiation = null))
        val result = extractor.extract(noDifferentiation)
        assertThat(result.extracted).isEmpty()
        assertThat(result.evaluation.warnings).isEmpty()
    }

    @Test
    fun `Should extract and curate IHC test from provided test results`() {
        val result = extractor.extract(EHR_PATIENT_RECORD_WITH_IHC_TEST)
        assertThat(result.extracted).containsExactly(PRIOR_IHC_TEST)
        assertThat(result.evaluation.warnings).isEmpty()
    }

    @Test
    fun `Should extract and curate IHC lines from tumor grade differentiation`() {
        every { molecularTestCuration.find(IHC_LINE) } returns setOf(
            MolecularTestConfig(
                input = IHC_LINE,
                curated = PRIOR_IHC_TEST
            )
        )
        val result = extractor.extract(EHR_PATIENT_RECORD_WITH_PATHOLOGY)
        assertThat(result.extracted).containsExactly(PRIOR_IHC_TEST)
        assertThat(result.evaluation.warnings).isEmpty()
    }

    @Test
    fun `Should ignore lines if ignored in curation`() {
        every { molecularTestCuration.find(IHC_LINE) } returns setOf(
            MolecularTestConfig(
                input = IHC_LINE,
                ignore = true
            )
        )
        val result =
            extractor.extract(EHR_PATIENT_RECORD_WITH_PATHOLOGY)
        assertThat(result.extracted).isEmpty()
        assertThat(result.evaluation.warnings).isEmpty()
    }

    @Test
    fun `Should give curation warnings for uncurated lines`() {
        every { molecularTestCuration.find(IHC_LINE) } returns emptySet()
        val result =
            extractor.extract(EHR_PATIENT_RECORD_WITH_PATHOLOGY)
        assertThat(result.extracted).isEmpty()
        assertThat(result.evaluation.warnings).containsExactly(
            CurationWarning(
                patientId = HASHED_ID_IN_BASE64,
                category = CurationCategory.MOLECULAR_TEST_IHC,
                feedInput = "HER2 immunohistochemie: negative",
                message = "Could not find molecular test ihc config for input 'HER2 immunohistochemie: negative'"
            )
        )
    }

    @Test
    fun `Should curate molecular tests from prior other conditions, supporting multiple configs per input, but ignore any curation warnings`() {
        val anotherMolecularTest = PRIOR_MOLECULAR_TEST.copy(item = "ERBB2")
        every { molecularTestCuration.find(PRIOR_CONDITION_INPUT) } returns setOf(
            MolecularTestConfig(
                input = PRIOR_CONDITION_INPUT,
                curated = PRIOR_MOLECULAR_TEST
            ),
            MolecularTestConfig(
                input = PRIOR_CONDITION_INPUT,
                curated = anotherMolecularTest
            )
        )
        val result =
            extractor.extract(
                EHR_PATIENT_RECORD.copy(
                    priorOtherConditions = listOf(
                        ProvidedPriorOtherCondition(
                            name = PRIOR_CONDITION_INPUT,
                            startDate = UNUSED_DATE
                        ),
                        ProvidedPriorOtherCondition(
                            name = "another prior condition",
                            startDate = UNUSED_DATE
                        )
                    )
                )
            )
        assertThat(result.extracted).containsExactly(PRIOR_MOLECULAR_TEST, anotherMolecularTest)
        assertThat(result.evaluation.warnings).isEmpty()
    }
}