package com.hartwig.actin.algo.evaluation.molecular

import com.hartwig.actin.TestPatientFactory
import com.hartwig.actin.algo.datamodel.EvaluationResult
import com.hartwig.actin.algo.evaluation.EvaluationAssert.assertMolecularEvaluation
import com.hartwig.actin.molecular.datamodel.driver.TestTranscriptImpactFactory
import com.hartwig.actin.molecular.datamodel.driver.TestVariantFactory
import com.hartwig.actin.molecular.datamodel.driver.TranscriptImpact
import org.junit.Test

private const val MATCHING_GENE = "gene A"

private const val MATCHING_PROTEIN_IMPACT = "V600E"

class GeneHasVariantWithProteinImpactTest {
    private val function = GeneHasVariantWithProteinImpact(MATCHING_GENE, listOf(MATCHING_PROTEIN_IMPACT, "V600K"))
    
    @Test
    fun `Should fail when gene not present`() {
        assertMolecularEvaluation(EvaluationResult.FAIL, function.evaluate(TestPatientFactory.createMinimalTestWGSPatientRecord()))
    }

    @Test
    fun `Should fail when no protein impacts configured`() {
        assertMolecularEvaluation(
            EvaluationResult.FAIL,
            function.evaluate(
                TestMolecularTestFactory.withVariant(TestVariantFactory.createMinimal().copy(gene = MATCHING_GENE, isReportable = true))
            )
        )
    }

    @Test
    fun `Should fail when no protein impacts match`() {
        assertMolecularEvaluation(
            EvaluationResult.FAIL,
            function.evaluate(
                TestMolecularTestFactory.withVariant(
                    TestVariantFactory.createMinimal().copy(
                        gene = MATCHING_GENE,
                        isReportable = true,
                        canonicalImpact = proteinImpact("V600P"),
                        otherImpacts = setOf(proteinImpact("V600P")),
                    )
                )
            )
        )
    }

    @Test
    fun `Should fail for incorrect gene with matching impact`() {
        assertMolecularEvaluation(
            EvaluationResult.FAIL,
            function.evaluate(
                TestMolecularTestFactory.withVariant(
                    TestVariantFactory.createMinimal().copy(
                        gene = "gene B",
                        isReportable = true,
                        canonicalImpact = proteinImpact(MATCHING_PROTEIN_IMPACT)
                    )

                )
            )
        )
    }

    @Test
    fun `Should pass for correct gene with matching canonical impact`() {
        assertMolecularEvaluation(
            EvaluationResult.PASS,
            function.evaluate(
                TestMolecularTestFactory.withVariant(
                    TestVariantFactory.createMinimal().copy(
                        gene = MATCHING_GENE,
                        isReportable = true,
                        clonalLikelihood = 1.0,
                        canonicalImpact = proteinImpact(MATCHING_PROTEIN_IMPACT)
                    )
                )
            )
        )
    }

    @Test
    fun `Should warn for correct gene with matching canonical impact but not reportable`() {
        assertMolecularEvaluation(
            EvaluationResult.WARN,
            function.evaluate(
                TestMolecularTestFactory.withVariant(
                    TestVariantFactory.createMinimal().copy(
                        gene = MATCHING_GENE,
                        isReportable = false,
                        canonicalImpact = proteinImpact(MATCHING_PROTEIN_IMPACT)
                    )
                )
            )
        )
    }

    @Test
    fun `Should warn for correct gene with matching canonical impact but subclonal`() {
        assertMolecularEvaluation(
            EvaluationResult.WARN,
            function.evaluate(
                TestMolecularTestFactory.withVariant(
                    TestVariantFactory.createMinimal().copy(
                        gene = MATCHING_GENE,
                        isReportable = true,
                        clonalLikelihood = 0.3,
                        canonicalImpact = proteinImpact(MATCHING_PROTEIN_IMPACT)
                    )
                )
            )
        )
    }

    @Test
    fun `Should warn for correct gene with matching non-canonical impact`() {
        assertMolecularEvaluation(
            EvaluationResult.WARN,
            function.evaluate(
                TestMolecularTestFactory.withVariant(
                    TestVariantFactory.createMinimal().copy(
                        gene = MATCHING_GENE,
                        isReportable = true,
                        canonicalImpact = proteinImpact("V600P"),
                        otherImpacts = setOf(proteinImpact("V600P"), proteinImpact(MATCHING_PROTEIN_IMPACT))
                    )
                )
            )
        )
    }

    private fun proteinImpact(hgvsProteinImpact: String): TranscriptImpact {
        return TestTranscriptImpactFactory.createMinimal().copy(hgvsProteinImpact = hgvsProteinImpact)
    }
}