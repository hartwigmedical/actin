package com.hartwig.actin.algo.evaluation.molecular

import com.hartwig.actin.algo.evaluation.EvaluationAssert.assertMolecularEvaluation
import com.hartwig.actin.datamodel.TestPatientFactory
import com.hartwig.actin.datamodel.algo.EvaluationResult
import com.hartwig.actin.datamodel.molecular.CodingEffect
import com.hartwig.actin.datamodel.molecular.driver.TestFusionFactory
import com.hartwig.actin.datamodel.molecular.driver.TestTranscriptImpactFactory
import com.hartwig.actin.datamodel.molecular.driver.TestVariantFactory
import org.junit.Test

private const val MATCHING_GENE = "gene A"

private val EXON_SKIPPING_FUSION = TestFusionFactory.createMinimal().copy(
    isReportable = true,
    geneStart = MATCHING_GENE,
    geneEnd = MATCHING_GENE,
    extendedFusionDetails =
    TestFusionFactory.createMinimalExtended().copy(fusedExonUp = 1, fusedExonDown = 3)
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
            EvaluationResult.FAIL,
            function.evaluate(
                MolecularTestFactory.withFusion(
                    EXON_SKIPPING_FUSION.copy(
                        extendedFusionDetails = TestFusionFactory.createMinimalExtended().copy(fusedExonDown = 5)
                    )
                )
            )
        )
    }
}