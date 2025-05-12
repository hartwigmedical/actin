package com.hartwig.actin.clinical.feed.standard.extraction

import com.hartwig.actin.clinical.curation.CurationDatabase
import com.hartwig.actin.clinical.curation.config.IhcTestConfig
import com.hartwig.actin.clinical.feed.standard.EhrTestData.createEhrPatientRecord
import com.hartwig.actin.clinical.feed.standard.HASHED_ID_IN_BASE64
import com.hartwig.actin.clinical.feed.standard.OTHER_CONDITION_INPUT
import com.hartwig.actin.datamodel.clinical.IhcTest
import com.hartwig.actin.datamodel.clinical.ingestion.CurationCategory
import com.hartwig.actin.datamodel.clinical.ingestion.CurationWarning
import com.hartwig.actin.datamodel.clinical.provided.ProvidedMolecularTest
import com.hartwig.actin.datamodel.clinical.provided.ProvidedMolecularTestResult
import com.hartwig.actin.datamodel.clinical.provided.ProvidedOtherCondition
import com.hartwig.actin.datamodel.clinical.provided.ProvidedPathologyReport
import io.mockk.every
import io.mockk.mockk
import java.time.LocalDate
import org.assertj.core.api.Assertions.assertThat
import org.junit.Test

private const val IHC_LINE = "HER2 immunohistochemie: negative"
private val IHC_TEST = IhcTest(item = "HER2", measure = "negative", impliesPotentialIndeterminateStatus = true)
private const val MICROSCOPIE_LINE = "TTF1 negatief"

private val PATHOLOGY_REPORT = ProvidedPathologyReport(
    lab = "lab",
    diagnosis = "diagnosis",
    reportRequested = true,
    rawPathologyReport = "Microscopie:\n$MICROSCOPIE_LINE\n\nConclusie:\n\nunrelated.\r\n\r\n\r\n$IHC_LINE\n\n"
)
private val IHC_TEST_EGFR =
    IhcTest(
        item = "EGFR",
        measure = "c.2573T>G",
        measureDate = LocalDate.parse("2024-03-25"),
        scoreText = "variant",
        impliesPotentialIndeterminateStatus = false
    )

private val EHR_PATIENT_RECORD = createEhrPatientRecord()
private val EHR_PATIENT_RECORD_WITH_PATHOLOGY =
    EHR_PATIENT_RECORD.copy(tumorDetails = EHR_PATIENT_RECORD.tumorDetails.copy(pathology = listOf(PATHOLOGY_REPORT)))
private val UNUSED_DATE = LocalDate.of(2024, 4, 15)


class StandardIhcTestExtractorTest {

    private val molecularTestCuration = mockk<CurationDatabase<IhcTestConfig>> {
        every { find(any()) } returns emptySet()
    }
    private val extractor = StandardIhcTestExtractor(molecularTestCuration)

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
            IhcTestConfig(
                input = IHC_LINE,
                curated = IHC_TEST
            )
        )
        val result = extractor.extract(EHR_PATIENT_RECORD_WITH_PATHOLOGY)
        assertThat(result.extracted).containsExactly(IHC_TEST)
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
        val anotherMolecularTest = IHC_TEST_EGFR.copy(item = "ERBB2")
        every { molecularTestCuration.find(OTHER_CONDITION_INPUT) } returns setOf(
            IhcTestConfig(
                input = OTHER_CONDITION_INPUT,
                curated = IHC_TEST_EGFR
            ),
            IhcTestConfig(
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
        assertThat(result.extracted).containsExactly(IHC_TEST_EGFR, anotherMolecularTest)
        assertThat(result.evaluation.warnings).isEmpty()
    }

    @Test
    fun `Should curate molecular tests from tumor grade differentiation lines, supporting multiple configs per input, but ignore any curation warnings`() {
        val anotherMolecularTest = IHC_TEST_EGFR.copy(item = "ERBB2")
        every { molecularTestCuration.find(MICROSCOPIE_LINE) } returns setOf(
            IhcTestConfig(
                input = MICROSCOPIE_LINE,
                curated = IHC_TEST_EGFR
            ),
            IhcTestConfig(
                input = MICROSCOPIE_LINE,
                curated = anotherMolecularTest
            )
        )
        val result =
            extractor.extract(
                EHR_PATIENT_RECORD.copy(
                    tumorDetails = EHR_PATIENT_RECORD.tumorDetails.copy(
                        pathology = listOf(
                            PATHOLOGY_REPORT.copy(
                                rawPathologyReport = PATHOLOGY_REPORT.rawPathologyReport.replace(
                                    IHC_LINE, ""
                                )
                            )
                        )
                    )
                )
            )
        assertThat(result.extracted).containsExactly(IHC_TEST_EGFR, anotherMolecularTest)
        assertThat(result.evaluation.warnings).isEmpty()
    }

    @Test
    fun `Should extract and curate IHC lines from molecular test ihc result`() {
        every { molecularTestCuration.find(IHC_LINE) } returns setOf(
            IhcTestConfig(
                input = IHC_LINE,
                curated = IHC_TEST
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
        assertThat(result.extracted).containsExactly(IHC_TEST)
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
        val firstIhc = ProvidedMolecularTestResult(ihcResult = "first IHC")
        val secondIhc = ProvidedMolecularTestResult(ihcResult = "second IHC")
        val record = EHR_PATIENT_RECORD.copy(
            molecularTests = listOf(
                ProvidedMolecularTest(
                    test = "test",
                    results = setOf(firstIhc, secondIhc)
                )
            )
        )

        val inputWithPatientAndIhc = "$HASHED_ID_IN_BASE64 | ${firstIhc.ihcResult}"
        returnIgnoreFromCurationDB(inputWithPatientAndIhc)

        val inputWithIhcOnly = secondIhc.ihcResult!!
        returnIgnoreFromCurationDB(inputWithIhcOnly)

        val result = extractor.extract(record)
        assertThat(result.extracted).isEmpty()
        assertThat(result.evaluation.warnings).isEmpty()
    }

    private fun returnIgnoreFromCurationDB(inputWithPatientAndIhc: String) {
        every { molecularTestCuration.find(inputWithPatientAndIhc) } returns setOf(
            IhcTestConfig(
                input = inputWithPatientAndIhc,
                ignore = true
            )
        )
    }
}