package com.hartwig.actin.algo.evaluation.molecular

import com.hartwig.actin.TestPatientFactory
import com.hartwig.actin.algo.datamodel.EvaluationResult
import com.hartwig.actin.algo.evaluation.EvaluationAssert.assertMolecularEvaluation
import com.hartwig.actin.molecular.datamodel.CodingEffect
import com.hartwig.actin.molecular.datamodel.TestPanelRecordFactory
import com.hartwig.actin.molecular.datamodel.driver.TestFusionFactory
import com.hartwig.actin.molecular.datamodel.driver.TestTranscriptImpactFactory
import com.hartwig.actin.molecular.datamodel.driver.TestVariantFactory
import com.hartwig.actin.molecular.datamodel.panel.archer.ArcherSkippedExonsExtraction
import org.junit.Test

private const val MATCHING_GENE = "gene A"

private val EXON_SKIPPING_FUSION = TestFusionFactory.createMinimal().copy(
    isReportable = true,
    geneStart = MATCHING_GENE,
    fusedExonUp = 1,
    geneEnd = MATCHING_GENE,
    fusedExonDown = 3
)

private val SPLICE_VARIANT = TestVariantFactory.createMinimal().copy(
    gene = MATCHING_GENE,
    isReportable = true,
    canonicalImpact = TestTranscriptImpactFactory.createMinimal().copy(affectedExon = 2, isSpliceRegion = true)
)

class GeneHasSpecificExonSkippingTest {

    val function = GeneHasSpecificExonSkipping(MATCHING_GENE, 2)

    @Test
    fun `Should be undetermined when no molecular history in patient record`() {
        assertMolecularEvaluation(
            EvaluationResult.UNDETERMINED, function.evaluate(
                TestPatientFactory.createEmptyMolecularTestPatientRecord()
            )
        )
    }

    @Test
    fun `Should fail when no variants in patient record`() {
        assertMolecularEvaluation(EvaluationResult.FAIL, function.evaluate(TestPatientFactory.createMinimalTestWGSPatientRecord()))
    }

    @Test
    fun `Should warn on splice variant in specific exon`() {
        assertMolecularEvaluation(EvaluationResult.WARN, function.evaluate(MolecularTestFactory.withVariant(SPLICE_VARIANT)))
    }

    @Test
    fun `Should warn on splice variant in specific exon with canonical impact`() {
        assertMolecularEvaluation(
            EvaluationResult.WARN,
            function.evaluate(
                MolecularTestFactory.withVariant(
                    SPLICE_VARIANT.copy(
                        canonicalImpact = TestTranscriptImpactFactory.createMinimal().copy(
                            affectedExon = 2, codingEffect = CodingEffect.SPLICE
                        )
                    )
                )
            )
        )
    }

    @Test
    fun `Should fail on splice variant in specific exon that is not reportable`() {
        assertMolecularEvaluation(
            EvaluationResult.FAIL, function.evaluate(MolecularTestFactory.withVariant(SPLICE_VARIANT.copy(isReportable = false)))
        )
    }

    @Test
    fun `Should pass on fusion skipping specific exon`() {
        assertMolecularEvaluation(EvaluationResult.PASS, function.evaluate(MolecularTestFactory.withFusion(EXON_SKIPPING_FUSION)))
    }

    @Test
    fun `Should fail on fusion skipping specific exon that is not reportable`() {
        assertMolecularEvaluation(
            EvaluationResult.FAIL, function.evaluate(MolecularTestFactory.withFusion(EXON_SKIPPING_FUSION.copy(isReportable = false)))
        )
    }

    @Test
    fun `Should fail on fusion skipping more than the specific exon`() {
        assertMolecularEvaluation(
            EvaluationResult.FAIL, function.evaluate(MolecularTestFactory.withFusion(EXON_SKIPPING_FUSION.copy(fusedExonDown = 5)))
        )
    }

    @Test
    fun `Should pass on exon skipping detected in archer panel for specific exon`() {
        assertMolecularEvaluation(
            EvaluationResult.PASS, function.evaluate(
                MolecularTestFactory.withMolecularTestsAndNoOrangeMolecular(
                    listOf(
                        archerPanelWithExonSkippingForGene(2, 2)
                    )
                )
            )
        )
    }

    private fun archerPanelWithExonSkippingForGene(start: Int, end: Int) = TestPanelRecordFactory.empty().copy(
        panelEvents = setOf(
            ArcherSkippedExonsExtraction(
                MATCHING_GENE,
                start,
                end
            )
        )
    )

    @Test
    fun `Should fail on exon skipping detected in archer panel for range including exon`() {
        assertMolecularEvaluation(
            EvaluationResult.FAIL, function.evaluate(
                MolecularTestFactory.withMolecularTestsAndNoOrangeMolecular(
                    listOf(archerPanelWithExonSkippingForGene(1, 3))
                )
            )
        )
    }

    @Test
    fun `Should fail on exon skipping detected in archer panel specific exon not matching`() {
        assertMolecularEvaluation(
            EvaluationResult.FAIL, function.evaluate(
                MolecularTestFactory.withMolecularTestsAndNoOrangeMolecular(
                    listOf(archerPanelWithExonSkippingForGene(3, 3))
                )
            )
        )
    }
}