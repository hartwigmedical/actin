package com.hartwig.actin.algo.evaluation.molecular

import com.google.common.collect.Lists
import com.hartwig.actin.TestPatientFactory
import com.hartwig.actin.algo.datamodel.EvaluationResult
import com.hartwig.actin.algo.evaluation.EvaluationAssert.assertMolecularEvaluation
import com.hartwig.actin.molecular.datamodel.driver.TestTranscriptImpactFactory
import com.hartwig.actin.molecular.datamodel.driver.TestVariantFactory
import org.junit.Test

private const val MATCHING_CODON = 100
private const val OTHER_CODON = 300
private const val TARGET_GENE = "gene A"

class GeneHasWgsVariantInCodonTest {
    private val function = GeneHasVariantInCodon(TARGET_GENE, Lists.newArrayList("A100", "B200"))
    
    @Test
    fun `Should fail when gene not present`() {
        assertMolecularEvaluation(EvaluationResult.FAIL, function.evaluate(TestPatientFactory.createMinimalTestWGSPatientRecord()))
    }

    @Test
    fun `Should fail when no codons configured`() {
        assertMolecularEvaluation(
            EvaluationResult.FAIL,
            function.evaluate(
                MolecularTestFactory.withVariant(TestVariantFactory.createMinimal().copy(isReportable = true, gene = TARGET_GENE))
            )
        )
    }

    @Test
    fun `Should fail when no codons match`() {
        assertMolecularEvaluation(
            EvaluationResult.FAIL,
            function.evaluate(
                MolecularTestFactory.withVariant(
                    TestVariantFactory.createMinimal().copy(
                        gene = TARGET_GENE, isReportable = true, canonicalImpact = impactWithCodon(OTHER_CODON)
                    )
                )
            )
        )
    }

    @Test
    fun `Should pass when reportable codon matches`() {
        assertMolecularEvaluation(
            EvaluationResult.PASS,
            function.evaluate(
                MolecularTestFactory.withVariant(
                    TestVariantFactory.createMinimal().copy(
                        gene = TARGET_GENE, isReportable = true, clonalLikelihood = 1.0, canonicalImpact = impactWithCodon(MATCHING_CODON)
                    )
                )
            )
        )
    }

    @Test
    fun `Should warn when codon matches but not reportable`() {
        assertMolecularEvaluation(
            EvaluationResult.WARN,
            function.evaluate(
                MolecularTestFactory.withVariant(
                    TestVariantFactory.createMinimal().copy(
                        gene = TARGET_GENE, isReportable = false, canonicalImpact = impactWithCodon(MATCHING_CODON)
                    )
                )
            )
        )
    }

    @Test
    fun `Should warn when reportable codon matches but subclonal`() {
        assertMolecularEvaluation(
            EvaluationResult.WARN,
            function.evaluate(
                MolecularTestFactory.withVariant(
                    TestVariantFactory.createMinimal().copy(
                        gene = TARGET_GENE, isReportable = true, clonalLikelihood = 0.3, canonicalImpact = impactWithCodon(MATCHING_CODON)
                    )
                )
            )
        )
    }

    @Test
    fun `Should warn when reportable codon matches but not on canonical transcript`() {
        assertMolecularEvaluation(
            EvaluationResult.WARN,
            function.evaluate(
                MolecularTestFactory.withVariant(
                    TestVariantFactory.createMinimal().copy(
                        gene = TARGET_GENE,
                        isReportable = true,
                        canonicalImpact = impactWithCodon(OTHER_CODON),
                        otherImpacts = setOf(impactWithCodon(OTHER_CODON), impactWithCodon(MATCHING_CODON))
                    )
                )
            )
        )
    }

    private fun impactWithCodon(affectedCodon: Int) = TestTranscriptImpactFactory.createMinimal().copy(affectedCodon = affectedCodon)
}