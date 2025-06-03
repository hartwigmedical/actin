package com.hartwig.actin.clinical.feed.emc.extraction

import com.hartwig.actin.clinical.ExtractionResult
import com.hartwig.actin.clinical.curation.CurationDatabase
import com.hartwig.actin.clinical.curation.TestCurationFactory
import com.hartwig.actin.clinical.curation.config.SequencingTestConfig
import com.hartwig.actin.clinical.curation.config.SequencingTestResultConfig
import com.hartwig.actin.datamodel.clinical.SequencedVariant
import com.hartwig.actin.datamodel.clinical.SequencingTest
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
private val SEQUENCING_TEST = SequencingTest(test = TEST, knownSpecifications = false)


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
        assertResultContains(result, null, 0)
    }

    @Test
    fun `Should return warning if sequence test is missing for curated results`() {
        every { testCuration.find(TEST) } returns emptySet()
        every { testResultCuration.find(TEST) } returns setOf(
            SequencingTestResultConfig(input = TEST, gene = GENE, hgvsCodingImpact = CODING)
        )

        val result = extractor.extract(PATIENT_ID, questionnaire)
        assertResultContains(
            result, null, 1, setOf("Could not find sequencing test config for input '$TEST'")
        )
    }

    @Test
    fun `Should return warning if sequence test results is missing for curated test`() {
        every { testResultCuration.find(TEST) } returns emptySet()

        val result = extractor.extract(PATIENT_ID, questionnaire)
        assertResultContains(
            result, null, 1, setOf("Could not find sequencing test result config for input '$TEST'")
        )
    }

    @Test
    fun `Should curate any free text results`() {
        every { testResultCuration.find(TEST) } returns setOf(
            SequencingTestResultConfig(input = TEST, gene = GENE, hgvsCodingImpact = CODING)
        )

        val result = extractor.extract(PATIENT_ID, questionnaire)
        assertResultContains(
            result = result,
            sequencingTest = SEQUENCING_TEST.copy(variants = setOf(SequencedVariant(gene = GENE, hgvsCodingImpact = CODING))),
            numberOfInputs = 1
        )
    }

    @Test
    fun `Should not return curation warnings for uncurated text`() {
        every { testCuration.find(TEST) } returns emptySet()
        every { testResultCuration.find(TEST) } returns emptySet()

        val result = extractor.extract(PATIENT_ID, questionnaire)
        assertResultContains(result, null, 0)
    }

    @Test
    fun `Should respect ignore flag when curating free text`() {
        every { testResultCuration.find(TEST) } returns setOf(SequencingTestResultConfig(input = TEST, ignore = true))
        val result = extractor.extract(PATIENT_ID, questionnaire)
        assertResultContains(result, null, 1)
    }

    @Test
    fun `Should allow for ignoring of full tests`() {
        every { testCuration.find(TEST) } returns setOf(SequencingTestConfig(input = TEST, ignore = true, curatedName = "<ignore>"))
        every { testResultCuration.find(TEST) } returns setOf(
            SequencingTestResultConfig(input = TEST, ignore = true, gene = GENE, hgvsCodingImpact = CODING)
        )

        val result = extractor.extract(PATIENT_ID, questionnaire)
        assertResultContains(result, null, 1)
    }

    @Test
    fun `Should allow for ignoring of individual test results`() {
        every { testResultCuration.find(TEST) } returns setOf(
            SequencingTestResultConfig(input = TEST, ignore = false, gene = GENE, hgvsCodingImpact = CODING),
            SequencingTestResultConfig(input = TEST, ignore = true, gene = GENE2, hgvsProteinImpact = PROTEIN)
        )

        val result = extractor.extract(PATIENT_ID, questionnaire.copy(ihcTestResults = listOf(TEST)))
        assertResultContains(
            result = result,
            sequencingTest = SEQUENCING_TEST.copy(variants = setOf(SequencedVariant(gene = GENE, hgvsCodingImpact = CODING))),
            numberOfInputs = 1
        )
    }

    @Test
    fun `Should curated multiple sequencing test results `() {
        every { testResultCuration.find(TEST) } returns setOf(
            SequencingTestResultConfig(input = TEST, ignore = false, gene = GENE, hgvsCodingImpact = CODING),
            SequencingTestResultConfig(input = TEST, ignore = false, gene = GENE2, hgvsProteinImpact = PROTEIN)
        )

        val result = extractor.extract(PATIENT_ID, questionnaire.copy(ihcTestResults = listOf(TEST)))
        assertResultContains(
            result = result,
            sequencingTest = SEQUENCING_TEST.copy(
                variants = setOf(
                    SequencedVariant(gene = GENE, hgvsCodingImpact = CODING), SequencedVariant(gene = GENE2, hgvsProteinImpact = PROTEIN)
                )
            ),
            numberOfInputs = 1
        )
    }

    private fun assertResultContains(
        result: ExtractionResult<List<SequencingTest>>,
        sequencingTest: SequencingTest?,
        numberOfInputs: Int,
        warnings: Set<String> = emptySet()
    ) {

        if (sequencingTest == null) {
            assertThat(result.extracted).isEmpty()
        } else {
            assertThat(result.extracted).hasSize(1)
            assertThat(result.extracted[0]).isEqualTo(sequencingTest)
        }
        assertThat(result.evaluation.warnings.size).isEqualTo(warnings.size)
        assertThat(result.evaluation.warnings.map { it.message }).containsAll(warnings)
        assertThat(result.evaluation.sequencingTestEvaluatedInputs.size).isEqualTo(numberOfInputs)
        assertThat(result.evaluation.sequencingTestResultEvaluatedInputs.size).isEqualTo(numberOfInputs)
    }
}