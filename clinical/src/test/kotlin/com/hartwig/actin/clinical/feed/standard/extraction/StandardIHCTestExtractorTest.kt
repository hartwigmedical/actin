package com.hartwig.actin.clinical.feed.standard.extraction

import com.hartwig.actin.clinical.curation.CurationDatabase
import com.hartwig.actin.clinical.curation.config.IHCTestConfig
import com.hartwig.actin.clinical.feed.standard.EhrTestData.createEhrPatientRecord
import com.hartwig.actin.clinical.feed.standard.HASHED_ID_IN_BASE64
import com.hartwig.actin.clinical.feed.standard.OTHER_CONDITION_INPUT
import com.hartwig.actin.datamodel.clinical.IHCTest
import com.hartwig.actin.datamodel.clinical.ingestion.CurationCategory
import com.hartwig.actin.datamodel.clinical.ingestion.CurationWarning
import com.hartwig.actin.datamodel.clinical.provided.ProvidedMolecularTest
import com.hartwig.actin.datamodel.clinical.provided.ProvidedMolecularTestResult
import com.hartwig.actin.datamodel.clinical.provided.ProvidedOtherCondition
import io.mockk.every
import io.mockk.mockk
import org.assertj.core.api.Assertions.assertThat
import org.junit.Test
import java.time.LocalDate

private const val IHC_LINE = "HER2 immunohistochemie: negative"
private val PRIOR_IHC_TEST =
    IHCTest(test = "IHC", item = "HER2", measure = "negative", impliesPotentialIndeterminateStatus = true)
private const val MICROSCOPIE_LINE = "TTF1 negatief"

private const val PATHOLOGY_REPORT =
    "Microscopie:\n$MICROSCOPIE_LINE\n\nConclusie:\n\nunrelated.\r\n\r\n\r\n$IHC_LINE\n\n"
private val MOLECULAR_TEST =
    IHCTest(
        test = "Archer FP Lung Target",
        item = "EGFR",
        measure = "c.2573T>G",
        measureDate = LocalDate.parse("2024-03-25"),
        scoreText = "variant",
        impliesPotentialIndeterminateStatus = false
    )

private val EHR_PATIENT_RECORD = createEhrPatientRecord()
private val EHR_PATIENT_RECORD_WITH_PATHOLOGY =
    EHR_PATIENT_RECORD.copy(tumorDetails = EHR_PATIENT_RECORD.tumorDetails.copy(tumorGradeDifferentiation = PATHOLOGY_REPORT))
private val UNUSED_DATE = LocalDate.of(2024, 4, 15)


class StandardIHCTestExtractorTest {

    private val molecularTestCuration = mockk<CurationDatabase<IHCTestConfig>> {
        every { find(any()) } returns emptySet()
    }
    private val extractor = StandardIHCTestExtractor(molecularTestCuration)

    @Test
    fun `Should return no molecular test configs when tumor differentiation is null`() {
        val noDifferentiation =
            EHR_PATIENT_RECORD.copy(tumorDetails = EHR_PATIENT_RECORD.tumorDetails.copy(tumorGradeDifferentiation = null))
        val result = extractor.extract(noDifferentiation)
        assertThat(result.extracted).isEmpty()
        assertThat(result.evaluation.warnings).isEmpty()
    }

    @Test
    fun `Should extract and curate IHC lines from tumor grade differentiation`() {
        every { molecularTestCuration.find(IHC_LINE) } returns setOf(
            IHCTestConfig(
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
        returnIgnoreFromCurationDB(IHC_LINE)
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
    fun `Should curate molecular tests from other conditions, supporting multiple configs per input, but ignore any curation warnings`() {
        val anotherMolecularTest = MOLECULAR_TEST.copy(item = "ERBB2")
        every { molecularTestCuration.find(OTHER_CONDITION_INPUT) } returns setOf(
            IHCTestConfig(
                input = OTHER_CONDITION_INPUT,
                curated = MOLECULAR_TEST
            ),
            IHCTestConfig(
                input = OTHER_CONDITION_INPUT,
                curated = anotherMolecularTest
            )
        )
        val result =
            extractor.extract(
                EHR_PATIENT_RECORD.copy(
                    priorOtherConditions = listOf(
                        ProvidedOtherCondition(
                            name = OTHER_CONDITION_INPUT,
                            startDate = UNUSED_DATE
                        ),
                        ProvidedOtherCondition(
                            name = "another prior condition",
                            startDate = UNUSED_DATE
                        )
                    )
                )
            )
        assertThat(result.extracted).containsExactly(MOLECULAR_TEST, anotherMolecularTest)
        assertThat(result.evaluation.warnings).isEmpty()
    }

    @Test
    fun `Should curate molecular tests from tumor grade differentiation lines, supporting multiple configs per input, but ignore any curation warnings`() {
        val anotherMolecularTest = MOLECULAR_TEST.copy(item = "ERBB2")
        every { molecularTestCuration.find(MICROSCOPIE_LINE) } returns setOf(
            IHCTestConfig(
                input = MICROSCOPIE_LINE,
                curated = MOLECULAR_TEST
            ),
            IHCTestConfig(
                input = MICROSCOPIE_LINE,
                curated = anotherMolecularTest
            )
        )
        val result =
            extractor.extract(
                EHR_PATIENT_RECORD.copy(
                    tumorDetails = EHR_PATIENT_RECORD.tumorDetails.copy(
                        tumorGradeDifferentiation = PATHOLOGY_REPORT.replace(
                            IHC_LINE, ""
                        )
                    )
                )
            )
        assertThat(result.extracted).containsExactly(MOLECULAR_TEST, anotherMolecularTest)
        assertThat(result.evaluation.warnings).isEmpty()
    }

    @Test
    fun `Should extract and curate IHC lines from molecular test ihc result`() {
        every { molecularTestCuration.find(IHC_LINE) } returns setOf(
            IHCTestConfig(
                input = IHC_LINE,
                curated = PRIOR_IHC_TEST
            )
        )
        val uncuratedInput = "uncurated"
        val result = extractor.extract(
            EHR_PATIENT_RECORD.copy(
                molecularTests = listOf(
                    ProvidedMolecularTest(
                        test = "IHC",
                        results = setOf(ProvidedMolecularTestResult(ihcResult = IHC_LINE))
                    ),
                    ProvidedMolecularTest(
                        test = "IHC",
                        results = setOf(ProvidedMolecularTestResult(ihcResult = uncuratedInput))
                    ),
                    ProvidedMolecularTest(
                        test = "NGS",
                        results = setOf(ProvidedMolecularTestResult(hgvsCodingImpact = "codingImpact"))
                    ),
                )
            )
        )
        assertThat(result.extracted).containsExactly(PRIOR_IHC_TEST)
        assertThat(result.evaluation.warnings).containsExactly(
            CurationWarning(
                patientId = HASHED_ID_IN_BASE64,
                category = CurationCategory.MOLECULAR_TEST_IHC,
                feedInput = uncuratedInput,
                message = "Could not find molecular test config for input 'uncurated'"
            )
        )
    }

    @Test
    fun `Should ignore lines if ignored in curation using ihc result alone or combined with patient id`() {
        val firstIHC = ProvidedMolecularTestResult(ihcResult = "first IHC")
        val secondIHC = ProvidedMolecularTestResult(ihcResult = "second IHC")
        val record = EHR_PATIENT_RECORD.copy(
            molecularTests = listOf(
                ProvidedMolecularTest(
                    test = "test",
                    results = setOf(firstIHC, secondIHC)
                )
            )
        )

        val inputWithPatientAndIHC = "$HASHED_ID_IN_BASE64 | ${firstIHC.ihcResult}"
        returnIgnoreFromCurationDB(inputWithPatientAndIHC)

        val inputWithIHCOnly = secondIHC.ihcResult!!
        returnIgnoreFromCurationDB(inputWithIHCOnly)

        val result = extractor.extract(record)
        assertThat(result.extracted).isEmpty()
        assertThat(result.evaluation.warnings).isEmpty()
    }

    private fun returnIgnoreFromCurationDB(inputWithPatientAndIHC: String) {
        every { molecularTestCuration.find(inputWithPatientAndIHC) } returns setOf(
            IHCTestConfig(
                input = inputWithPatientAndIHC,
                ignore = true
            )
        )
    }
}