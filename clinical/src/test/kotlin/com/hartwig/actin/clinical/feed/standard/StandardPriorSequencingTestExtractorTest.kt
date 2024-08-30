package com.hartwig.actin.clinical.feed.standard

import com.hartwig.actin.clinical.ExtractionResult
import com.hartwig.actin.clinical.curation.CurationCategory
import com.hartwig.actin.clinical.curation.CurationDatabase
import com.hartwig.actin.clinical.curation.CurationWarning
import com.hartwig.actin.clinical.curation.config.SequencingTestConfig
import com.hartwig.actin.datamodel.clinical.PriorSequencingTest
import com.hartwig.actin.datamodel.clinical.SequencedAmplification
import com.hartwig.actin.datamodel.clinical.SequencedDeletedGene
import com.hartwig.actin.datamodel.clinical.SequencedFusion
import com.hartwig.actin.datamodel.clinical.SequencedSkippedExons
import com.hartwig.actin.datamodel.clinical.SequencedVariant
import io.mockk.every
import io.mockk.mockk
import org.assertj.core.api.Assertions.assertThat
import org.junit.Test
import java.time.LocalDate

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
private const val FREE_TEXT = "free text"

class StandardPriorSequencingTestExtractorTest {

    val curation = mockk<CurationDatabase<SequencingTestConfig>>()
    val extractor = StandardPriorSequencingTestExtractor(curation)

    @Test
    fun `Should return empty list when no provided molecular tests`() {
        val result = extractor.extract(EhrTestData.createEhrPatientRecord().copy(molecularTests = emptyList()))
        assertThat(result.extracted).isEmpty()
        assertThat(result.evaluation.warnings).isEmpty()
    }

    @Test
    fun `Should extract sequencing with test, date, and tested genes`() {
        val result = extractor.extract(
            EhrTestData.createEhrPatientRecord().copy(
                molecularTests = listOf(
                    BASE_MOLECULAR_TEST.copy(testedGenes = setOf(GENE))
                )
            )
        )
        assertResultContains(result, BASE_PRIOR_SEQUENCING.copy(testedGenes = setOf(GENE)))
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
                variants = setOf(SequencedVariant(gene = GENE, hgvsCodingImpact = CODING, hgvsProteinImpact = PROTEIN))
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
            result, BASE_PRIOR_SEQUENCING.copy(fusions = setOf(SequencedFusion(geneUp = FUSION_GENE_UP, geneDown = FUSION_GENE_DOWN)))
        )
    }

    @Test
    fun `Should extract sequencing with amplifications`() {
        val result = extractionResult(
            ProvidedMolecularTestResult(
                amplifiedGene = AMPLIFIED_GENE
            )
        )
        assertResultContains(result, BASE_PRIOR_SEQUENCING.copy(amplifications = setOf(SequencedAmplification(gene = AMPLIFIED_GENE))))
    }

    @Test
    fun `Should extract sequencing with exon skipping`() {
        val result = extractionResult(ProvidedMolecularTestResult(gene = GENE, exonSkipStart = 1, exonSkipEnd = 2))
        assertResultContains(
            result, BASE_PRIOR_SEQUENCING.copy(
                skippedExons = setOf(
                    SequencedSkippedExons(gene = GENE, exonStart = 1, exonEnd = 2)
                )
            )
        )
    }

    @Test
    fun `Should extract sequencing with TMB and MSI`() {
        val result = extractionResult(ProvidedMolecularTestResult(tmb = 1.0, msi = true))
        assertResultContains(
            result, BASE_PRIOR_SEQUENCING.copy(
                tumorMutationalBurden = 1.0,
                isMicrosatelliteUnstable = true
            )
        )
    }

    @Test
    fun `Should extract sequenced deleted genes`() {
        val result = extractionResult(ProvidedMolecularTestResult(deletedGene = GENE))
        assertResultContains(
            result, BASE_PRIOR_SEQUENCING.copy(
                deletedGenes = setOf(SequencedDeletedGene(GENE))
            )
        )
    }

    @Test
    fun `Should curate any free text results`() {
        every { curation.find(FREE_TEXT) } returns setOf(
            SequencingTestConfig(
                input = FREE_TEXT,
                curated = ProvidedMolecularTestResult(gene = GENE, hgvsCodingImpact = CODING)
            )
        )
        val result = extractionResult(ProvidedMolecularTestResult(freeText = FREE_TEXT))
        assertResultContains(
            result, BASE_PRIOR_SEQUENCING.copy(
                variants = setOf(SequencedVariant(gene = GENE, hgvsCodingImpact = CODING))
            )
        )
    }

    @Test
    fun `Should return curation warnings for uncurated free text when all other fields are null`() {
        every { curation.find(FREE_TEXT) } returns emptySet()
        val result = extractionResult(ProvidedMolecularTestResult(freeText = FREE_TEXT))
        assertThat(result.evaluation.warnings).hasSize(1)
        assertThat(result.evaluation.warnings.first()).isEqualTo(
            CurationWarning(
                patientId = HASHED_ID_IN_BASE64,
                category = CurationCategory.SEQUENCING_TEST,
                feedInput = FREE_TEXT,
                message = "Could not find sequencing test config for input '$FREE_TEXT'"
            )
        )
    }

    @Test
    fun `Should not return curation warnings for uncurated free text when all other fields are null`() {
        every { curation.find(FREE_TEXT) } returns emptySet()
        val result = extractionResult(ProvidedMolecularTestResult(gene = GENE, freeText = FREE_TEXT))
        assertThat(result.evaluation.warnings).isEmpty()
    }

    @Test
    fun `Should respect ignore flag when curating free text`() {
        every { curation.find(FREE_TEXT) } returns setOf(
            SequencingTestConfig(
                input = FREE_TEXT,
                ignore = true
            )
        )
        val result = extractionResult(ProvidedMolecularTestResult(gene = GENE, freeText = FREE_TEXT))
        assertThat(result.evaluation.warnings).isEmpty()
        assertResultContains(result, BASE_PRIOR_SEQUENCING)
    }

    private fun extractionResult(result: ProvidedMolecularTestResult) = extractor.extract(
        EhrTestData.createEhrPatientRecord().copy(molecularTests = listOf(BASE_MOLECULAR_TEST.copy(results = setOf(result))))
    )

    private fun assertResultContains(result: ExtractionResult<List<PriorSequencingTest>>, priorSequencingTest: PriorSequencingTest) {
        assertThat(result.extracted).hasSize(1)
        assertThat(result.extracted[0]).isEqualTo(priorSequencingTest)
        assertThat(result.evaluation.warnings).isEmpty()
    }
}