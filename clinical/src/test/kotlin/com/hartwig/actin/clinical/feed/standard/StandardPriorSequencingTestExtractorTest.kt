package com.hartwig.actin.clinical.feed.standard

import com.hartwig.actin.clinical.ExtractionResult
import com.hartwig.actin.clinical.datamodel.PriorSequencingTest
import com.hartwig.actin.clinical.datamodel.SequencedAmplification
import com.hartwig.actin.clinical.datamodel.SequencedExonSkip
import com.hartwig.actin.clinical.datamodel.SequencedFusion
import com.hartwig.actin.clinical.datamodel.SequencedVariant
import java.time.LocalDate
import org.assertj.core.api.Assertions.assertThat
import org.junit.Test

private const val TEST = "test"
private const val GENE = "gene"
private const val CODING = "coding"
private val TEST_DATE = LocalDate.of(2024, 7, 25)
private const val PROTEIN = "protein"
private val BASE_MOLECULAR_TEST = ProvidedMolecularTest(
    test = TEST, date = TEST_DATE, results = emptySet()
)
private val BASE_PRIOR_SEQUENCING = PriorSequencingTest(
    test = TEST,
    date = TEST_DATE,
)
private const val FUSION_GENE_UP = "fusionUp"
private const val FUSION_GENE_DOWN = "fusionDown"
private const val AMPLIFIED_GENE = "amplifiedGene"
private const val AMPLIFIED_CHROMOSOME = "amplifiedChromosome"

class StandardPriorSequencingTestExtractorTest {

    val extractor = StandardPriorSequencingTestExtractor()

    @Test
    fun `Should return empty list when no provided molecular tests`() {
        val result = extractor.extract(EhrTestData.createEhrPatientRecord().copy(molecularTestHistory = emptyList()))
        assertThat(result.extracted).isEmpty()
        assertThat(result.evaluation.warnings).isEmpty()
    }

    @Test
    fun `Should extract sequencing with test, date, tested genes`() {
        val result = extractor.extract(
            EhrTestData.createEhrPatientRecord().copy(molecularTestHistory = listOf(BASE_MOLECULAR_TEST.copy(testedGenes = setOf(GENE))))
        )
        assertResultContains(result, BASE_PRIOR_SEQUENCING.copy(genesTested = setOf(GENE)))
    }

    @Test
    fun `Should extract sequencing with variants`() {
        val result = extractionResult(
            ProvidedMolecularTestResult(
                gene = GENE, hgvsCodingImpact = CODING, hgvsProteinImpact = PROTEIN
            )
        )
        assertResultContains(
            result, BASE_PRIOR_SEQUENCING.copy(
                variants = setOf(SequencedVariant(GENE, hgvsCodingImpact = CODING, hgvsProteinImpact = PROTEIN))
            )
        )
    }

    @Test
    fun `Should extract sequencing with fusions`() {
        val result = extractionResult(
            ProvidedMolecularTestResult(
                fusionGeneUp = FUSION_GENE_UP, fusionGeneDown = FUSION_GENE_DOWN
            )
        )
        assertResultContains(
            result,
            BASE_PRIOR_SEQUENCING.copy(fusions = setOf(SequencedFusion(geneUp = FUSION_GENE_UP, geneDown = FUSION_GENE_DOWN)))
        )
    }

    @Test
    fun `Should extract sequencing with amplifications`() {
        val result = extractionResult(
            ProvidedMolecularTestResult(
                amplifiedGene = AMPLIFIED_GENE, amplifiedChromosome = AMPLIFIED_CHROMOSOME
            )
        )
        assertResultContains(
            result,
            BASE_PRIOR_SEQUENCING.copy(
                amplifications = setOf(
                    SequencedAmplification(
                        gene = AMPLIFIED_GENE, chromosome = AMPLIFIED_CHROMOSOME
                    )
                )
            )
        )
    }

    @Test
    fun `Should extract sequencing with exon skipping`() {
        val result = extractionResult(
            ProvidedMolecularTestResult(
                gene = GENE, exonsSkipStart = 1, exonsSkipEnd = 2
            )
        )
        assertResultContains(
            result,
            BASE_PRIOR_SEQUENCING.copy(
                exonSkips = setOf(
                    SequencedExonSkip(gene = GENE, exonStart = 1, exonEnd = 2)
                )
            )
        )
    }

    private fun extractionResult(result: ProvidedMolecularTestResult) = extractor.extract(
        EhrTestData.createEhrPatientRecord().copy(molecularTestHistory = listOf(BASE_MOLECULAR_TEST.copy(results = setOf(result))))
    )

    private fun assertResultContains(result: ExtractionResult<List<PriorSequencingTest>>, priorSequencingTest: PriorSequencingTest) {
        assertThat(result.extracted).hasSize(1)
        assertThat(result.extracted[0]).isEqualTo(priorSequencingTest)
        assertThat(result.evaluation.warnings).isEmpty()
    }
}