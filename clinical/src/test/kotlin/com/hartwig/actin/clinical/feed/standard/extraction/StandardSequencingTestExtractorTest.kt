package com.hartwig.actin.clinical.feed.standard.extraction

import com.hartwig.actin.clinical.curation.CurationDatabase
import com.hartwig.actin.clinical.curation.config.SequencingTestConfig
import com.hartwig.actin.clinical.curation.config.SequencingTestResultConfig
import com.hartwig.actin.clinical.feed.standard.FeedTestData
import com.hartwig.actin.clinical.feed.standard.HASHED_ID_IN_BASE64
import com.hartwig.actin.datamodel.clinical.SequencedAmplification
import com.hartwig.actin.datamodel.clinical.SequencedDeletion
import com.hartwig.actin.datamodel.clinical.SequencedFusion
import com.hartwig.actin.datamodel.clinical.SequencedSkippedExons
import com.hartwig.actin.datamodel.clinical.SequencedVariant
import com.hartwig.actin.datamodel.clinical.SequencingTest
import com.hartwig.actin.datamodel.clinical.ingestion.CurationCategory
import com.hartwig.actin.datamodel.clinical.ingestion.CurationWarning
import com.hartwig.feed.datamodel.FeedSequencingTest
import io.mockk.every
import io.mockk.mockk
import java.time.LocalDate
import org.assertj.core.api.Assertions.assertThat
import org.junit.Test

private const val TEST = "test"
private const val CURATED_TEST = "test"
private const val GENE = "gene"
private const val CODING = "coding"
private val TEST_DATE = LocalDate.of(2024, 7, 25)
private const val PROTEIN = "protein"
private val BASE_MOLECULAR_TEST = FeedSequencingTest(
    name = TEST, date = TEST_DATE, results = emptyList(), testedGenes = listOf(GENE), knownSpecifications = true
)
private val BASE_SEQUENCING_TEST = SequencingTest(test = TEST, date = TEST_DATE, knownSpecifications = true)
private const val FREE_TEXT = "free text"
private val PATIENT_WITH_TEST_RESULT = FeedTestData.FEED_PATIENT_RECORD.copy(
    sequencingTests = listOf(BASE_MOLECULAR_TEST.copy(results = listOf(FREE_TEXT)))
)
private val SEQUENCING_TEST_CURATION_WARNING = CurationWarning(
    patientId = HASHED_ID_IN_BASE64,
    category = CurationCategory.SEQUENCING_TEST,
    feedInput = TEST,
    message = "Could not find sequencing test config for input 'test'"
)

class StandardSequencingTestExtractorTest {

    private val testCuration = mockk<CurationDatabase<SequencingTestConfig>> {
        every { find(TEST) } returns setOf(SequencingTestConfig(TEST, false, CURATED_TEST))
    }
    private val testResultCuration = mockk<CurationDatabase<SequencingTestResultConfig>>()
    val extractor = StandardSequencingTestExtractor(testCuration, testResultCuration)

    @Test
    fun `Should return empty list when no provided molecular tests`() {
        val result = extractor.extract(FeedTestData.FEED_PATIENT_RECORD)
        assertThat(result.extracted).isEmpty()
        assertThat(result.evaluation.warnings).isEmpty()
    }

    @Test
    fun `Should return curation warning when test name is not curated`() {
        every { testCuration.find(TEST) } returns emptySet()
        val result = extractor.extract(FeedTestData.FEED_PATIENT_RECORD.copy(sequencingTests = listOf(BASE_MOLECULAR_TEST)))
        assertThat(result.extracted).isEmpty()
        assertThat(result.evaluation.warnings).containsExactly(SEQUENCING_TEST_CURATION_WARNING)
    }

    @Test
    fun `Should return curation warnings for test and results when neither is curated`() {
        every { testCuration.find(TEST) } returns emptySet()
        setUpSequencingTestResultCuration()
        with(extractedResult()) {
            assertThat(extracted).isEmpty()
            assertThat(evaluation.warnings).containsExactly(
                SEQUENCING_TEST_CURATION_WARNING,
                CurationWarning(
                    patientId = HASHED_ID_IN_BASE64,
                    category = CurationCategory.SEQUENCING_TEST_RESULT,
                    feedInput = FREE_TEXT,
                    message = "Could not find sequencing test result config for input '$FREE_TEXT'"
                )
            )
        }
    }

    @Test
    fun `Should curate test name and extract sequencing with test name and date, even when no results present`() {
        setUpSequencingTestResultCuration()
        assertThat(extractedResult().extracted).containsExactly(SequencingTest(CURATED_TEST, TEST_DATE, knownSpecifications = true))
    }

    @Test
    fun `Should extract sequencing with variants`() {
        setUpSequencingTestResultCuration(
            SequencingTestResultConfig(input = FREE_TEXT, gene = GENE, hgvsCodingImpact = CODING, hgvsProteinImpact = PROTEIN)
        )
        assertResultContains(
            BASE_SEQUENCING_TEST.copy(
                variants = setOf(SequencedVariant(gene = GENE, hgvsCodingImpact = CODING, hgvsProteinImpact = PROTEIN))
            )
        )
    }

    @Test
    fun `Should extract sequencing with fusions`() {
        val fusionGeneUp = "fusionUp"
        val fusionGeneDown = "fusionDown"
        setUpSequencingTestResultCuration(
            SequencingTestResultConfig(input = FREE_TEXT, fusionGeneUp = fusionGeneUp, fusionGeneDown = fusionGeneDown)
        )
        assertResultContains(BASE_SEQUENCING_TEST.copy(fusions = setOf(SequencedFusion(geneUp = fusionGeneUp, geneDown = fusionGeneDown))))
    }

    @Test
    fun `Should extract sequencing with amplifications`() {
        val amplifiedGene = "amplifiedGene"
        setUpSequencingTestResultCuration(SequencingTestResultConfig(input = FREE_TEXT, amplifiedGene = amplifiedGene))
        assertResultContains(BASE_SEQUENCING_TEST.copy(amplifications = setOf(SequencedAmplification(gene = amplifiedGene))))
    }

    @Test
    fun `Should extract sequencing with exon skipping`() {
        setUpSequencingTestResultCuration(SequencingTestResultConfig(input = FREE_TEXT, gene = GENE, exonSkipStart = 1, exonSkipEnd = 2))
        assertResultContains(
            BASE_SEQUENCING_TEST.copy(skippedExons = setOf(SequencedSkippedExons(gene = GENE, exonStart = 1, exonEnd = 2)))
        )
    }

    @Test
    fun `Should extract sequencing with TMB and MSI`() {
        setUpSequencingTestResultCuration(SequencingTestResultConfig(input = FREE_TEXT, tmb = 1.0, msi = true))
        assertResultContains(BASE_SEQUENCING_TEST.copy(tumorMutationalBurden = 1.0, isMicrosatelliteUnstable = true))
    }

    @Test
    fun `Should extract sequenced deleted genes`() {
        setUpSequencingTestResultCuration(SequencingTestResultConfig(input = FREE_TEXT, deletedGene = GENE))
        assertResultContains(BASE_SEQUENCING_TEST.copy(deletions = setOf(SequencedDeletion(GENE))))
    }

    @Test
    fun `Should curate any free text results`() {
        setUpSequencingTestResultCuration(SequencingTestResultConfig(input = FREE_TEXT, gene = GENE, hgvsCodingImpact = CODING))
        assertResultContains(BASE_SEQUENCING_TEST.copy(variants = setOf(SequencedVariant(gene = GENE, hgvsCodingImpact = CODING))))
    }

    @Test
    fun `Should return curation warnings for uncurated free text`() {
        setUpSequencingTestResultCuration()
        with(extractedResult()) {
            assertThat(evaluation.warnings).hasSize(1)
            assertThat(evaluation.warnings.first()).isEqualTo(
                CurationWarning(
                    patientId = HASHED_ID_IN_BASE64,
                    category = CurationCategory.SEQUENCING_TEST_RESULT,
                    feedInput = FREE_TEXT,
                    message = "Could not find sequencing test result config for input '$FREE_TEXT'"
                )
            )
        }
    }

    @Test
    fun `Should respect ignore flag when curating free text`() {
        setUpSequencingTestResultCuration(
            SequencingTestResultConfig(
                input = FREE_TEXT,
                ignore = true
            )
        )
        with(extractedResult()) {
            assertThat(evaluation.warnings).isEmpty()
            assertThat(extracted.isEmpty())
        }
    }

    @Test
    fun `Should allow for ignoring of full tests`() {
        every { testCuration.find(TEST) } returns setOf(
            SequencingTestConfig(input = TEST, ignore = true, curatedName = "<ignore>")
        )
        assertThat(extractedResult().extracted).isEmpty()
    }

    @Test
    fun `Should allow for ignoring of individual test results`() {
        setUpSequencingTestResultCuration(
            SequencingTestResultConfig(input = FREE_TEXT, ignore = true, gene = GENE, hgvsCodingImpact = CODING),
            SequencingTestResultConfig(input = FREE_TEXT, ignore = false, gene = GENE, hgvsProteinImpact = PROTEIN)
        )
        assertThat(extractedResult().extracted[0].variants).containsExactly(SequencedVariant(gene = GENE, hgvsProteinImpact = PROTEIN))
    }

    private fun setUpSequencingTestResultCuration(vararg curationConfigs: SequencingTestResultConfig) {
        every { testResultCuration.find(FREE_TEXT) } returns curationConfigs.toSet()
    }

    private fun extractedResult() = extractor.extract(PATIENT_WITH_TEST_RESULT)

    private fun assertResultContains(sequencingTest: SequencingTest) = with(extractedResult()) {
        assertThat(extracted).containsExactly(sequencingTest)
        assertThat(evaluation.warnings).isEmpty()
    }
}