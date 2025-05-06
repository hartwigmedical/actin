package com.hartwig.actin.clinical.feed.emc.extraction

import com.hartwig.actin.clinical.ExtractionResult
import com.hartwig.actin.clinical.curation.CurationDatabase
import com.hartwig.actin.clinical.curation.TestCurationFactory
import com.hartwig.actin.clinical.curation.config.SequencingTestConfig
import com.hartwig.actin.clinical.curation.config.SequencingTestResultConfig
import com.hartwig.actin.datamodel.clinical.SequencedVariant
import com.hartwig.actin.datamodel.clinical.SequencingTest
import com.hartwig.actin.datamodel.clinical.provided.ProvidedMolecularTestResult
import io.mockk.every
import io.mockk.mockk
import org.assertj.core.api.Assertions.assertThat
import org.junit.Test

private const val PATIENT_ID = "patient1"
private const val TEST = "test"
private const val CURATED_TEST = "test"
private const val GENE = "gene1"
private const val GENE2 = "gene2"
private const val CODING = "coding"
private const val PROTEIN = "protein"
private val SEQUENCING_TEST = SequencingTest(test = TEST)


class SequencingTestExtractorTest {

    private val testCuration = mockk<CurationDatabase<SequencingTestConfig>> {
        every { find(TEST) } returns setOf(SequencingTestConfig(TEST, false, CURATED_TEST))
    }
    private val testResultCuration = mockk<CurationDatabase<SequencingTestResultConfig>>()
    private val extractor = SequencingTestExtractor(testCuration, testResultCuration)
    private val questionnaire = TestCurationFactory.emptyQuestionnaire().copy(ihcTestResults = listOf(TEST))

    @Test
    fun `Should return empty list when no provided molecular tests`() {

        val result = extractor.extract(PATIENT_ID, questionnaire.copy(ihcTestResults = emptyList()))
        assertThat(result.extracted).isEmpty()
        assertThat(result.evaluation.warnings).isEmpty()
    }

    @Test
    fun `Should curate any free text results using default test name`() {
        every { testCuration.find(TEST) } returns emptySet()
        every { testResultCuration.find(TEST) } returns setOf(
            SequencingTestResultConfig(
                input = TEST,
                curated = ProvidedMolecularTestResult(gene = GENE, hgvsCodingImpact = CODING)
            )
        )
        val result = extractor.extract(PATIENT_ID, questionnaire)

        assertResultContains(
            result, SEQUENCING_TEST.copy(
                test = "Unknown test",
                variants = setOf(SequencedVariant(gene = GENE, hgvsCodingImpact = CODING))
            )
        )
    }

    @Test
    fun `Should curate any free text results`() {
        every { testResultCuration.find(TEST) } returns setOf(
            SequencingTestResultConfig(
                input = TEST,
                curated = ProvidedMolecularTestResult(gene = GENE, hgvsCodingImpact = CODING)
            )
        )
        val result = extractor.extract(PATIENT_ID, questionnaire)

        assertResultContains(
            result, SEQUENCING_TEST.copy(
                variants = setOf(SequencedVariant(gene = GENE, hgvsCodingImpact = CODING))
            )
        )
    }

    @Test
    fun `Should not return curation warnings for uncurated text`() {
        every { testResultCuration.find(TEST) } returns emptySet()
        val result = extractor.extract(PATIENT_ID, questionnaire)
        assertThat(result.evaluation.warnings).isEmpty()
    }


    @Test
    fun `Should respect ignore flag when curating free text`() {
        every { testResultCuration.find(TEST) } returns setOf(
            SequencingTestResultConfig(
                input = TEST,
                ignore = true
            )
        )
        val result = extractor.extract(PATIENT_ID, questionnaire)
        assertThat(result.evaluation.warnings).isEmpty()
        assertThat(result.extracted.isEmpty())
    }

    @Test
    fun `Should allow for ignoring of full tests`() {
        every { testCuration.find(TEST) } returns setOf(
            SequencingTestConfig(
                input = TEST,
                ignore = true,
                curatedName = "<ignore>"
            )
        )
        val result = extractor.extract(PATIENT_ID, questionnaire)
        assertThat(result.extracted).isEmpty()
    }

    @Test
    fun `Should allow for ignoring of individual test results`() {
        every { testResultCuration.find(TEST) } returns setOf(
            SequencingTestResultConfig(
                input = TEST,
                ignore = false,
                curated = ProvidedMolecularTestResult(gene = GENE, hgvsCodingImpact = CODING)
            ),
            SequencingTestResultConfig(
                input = TEST,
                ignore = true,
                curated = ProvidedMolecularTestResult(gene = GENE2, hgvsProteinImpact = PROTEIN)
            )
        )
        val result = extractor.extract(PATIENT_ID, questionnaire.copy(ihcTestResults = listOf(TEST)))
        assertThat(result.extracted.size).isEqualTo(1)
        assertThat(result.extracted[0].variants).containsExactly(SequencedVariant(gene = GENE, hgvsCodingImpact = CODING))
    }

    @Test
    fun `Should curated multiple sequencing test results `() {
        every { testResultCuration.find(TEST) } returns setOf(
            SequencingTestResultConfig(
                input = TEST,
                ignore = false,
                curated = ProvidedMolecularTestResult(gene = GENE, hgvsCodingImpact = CODING)
            ),
            SequencingTestResultConfig(
                input = TEST,
                ignore = false,
                curated = ProvidedMolecularTestResult(gene = GENE2, hgvsProteinImpact = PROTEIN)
            )
        )
        val result = extractor.extract(PATIENT_ID, questionnaire.copy(ihcTestResults = listOf(TEST)))
        assertThat(result.extracted.size).isEqualTo(1)
        assertThat(result.extracted[0].variants).containsExactly(
            SequencedVariant(gene = GENE, hgvsCodingImpact = CODING),
            SequencedVariant(gene = GENE2, hgvsProteinImpact = PROTEIN)
        )
    }

    private fun assertResultContains(result: ExtractionResult<List<SequencingTest>>, sequencingTest: SequencingTest) {
        assertThat(result.extracted).hasSize(1)
        assertThat(result.extracted[0]).isEqualTo(sequencingTest)
        assertThat(result.evaluation.warnings).isEmpty()
    }
}