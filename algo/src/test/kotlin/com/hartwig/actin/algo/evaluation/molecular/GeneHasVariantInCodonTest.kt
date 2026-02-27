package com.hartwig.actin.algo.evaluation.molecular

import com.hartwig.actin.algo.evaluation.EvaluationAssert.assertMolecularEvaluation
import com.hartwig.actin.datamodel.TestPatientFactory
import com.hartwig.actin.datamodel.algo.EvaluationResult
import com.hartwig.actin.datamodel.molecular.TestMolecularFactory
import com.hartwig.actin.datamodel.molecular.driver.TestTranscriptVariantImpactFactory
import com.hartwig.actin.datamodel.molecular.driver.TestVariantFactory
import org.assertj.core.api.Assertions.assertThat
import org.junit.Test

private const val MATCHING_CODON = 100
private const val OTHER_CODON = 300
private const val TARGET_GENE = "gene A"

class GeneHasVariantInCodonTest {

    private val function = GeneHasVariantInCodon(TARGET_GENE, listOf("A100", "B200"))

    @Test
    fun `Should fail when gene not present`() {
        assertMolecularEvaluation(EvaluationResult.FAIL, function.evaluate(TestPatientFactory.createMinimalTestWGSPatientRecord()))
    }

    @Test
    fun `Should fail when no codons configured`() {
        assertMolecularEvaluation(
            EvaluationResult.FAIL,
            function.evaluate(
                MolecularTestFactory.withVariant(
                    TestVariantFactory.createMinimal()
                        .copy(isReportable = true, gene = TARGET_GENE)
                )
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
                        gene = TARGET_GENE,
                        isReportable = true,
                        canonicalImpact = impactWithCodon(OTHER_CODON)
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
                        gene = TARGET_GENE,
                        isReportable = true,
                        canonicalImpact = impactWithCodon(MATCHING_CODON),
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
    fun `Should warn when reportable codon matches but known subclonal`() {
        assertMolecularEvaluation(
            EvaluationResult.WARN,
            function.evaluate(
                MolecularTestFactory.withVariant(
                    TestVariantFactory.createMinimal().copy(
                        gene = TARGET_GENE,
                        isReportable = true,
                        canonicalImpact = impactWithCodon(MATCHING_CODON),
                        clonalLikelihood = 0.3
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
                        otherImpacts = setOf(impactWithCodon(OTHER_CODON), impactWithCodon(MATCHING_CODON)),
                    )
                )
            )
        )
    }

    @Test
    fun `Should warn when reportable codon matches but also variant on non-canonical transcript`() {
        assertMolecularEvaluation(
            EvaluationResult.WARN,
            function.evaluate(
                MolecularTestFactory.withDrivers(
                    TestVariantFactory.createMinimal().copy(
                        gene = TARGET_GENE,
                        isReportable = true,
                        canonicalImpact = impactWithCodon(MATCHING_CODON)
                    ),
                    TestVariantFactory.createMinimal().copy(
                        gene = TARGET_GENE,
                        isReportable = true,
                        canonicalImpact = impactWithCodon(OTHER_CODON),
                        otherImpacts = setOf(impactWithCodon(MATCHING_CODON))
                    )
                )
            )
        )
    }

    @Test
    fun `Should warn when reportable codon matches but also subclonal variant`() {
        assertMolecularEvaluation(
            EvaluationResult.WARN,
            function.evaluate(
                MolecularTestFactory.withDrivers(
                    TestVariantFactory.createMinimal().copy(
                        gene = TARGET_GENE,
                        isReportable = true,
                        canonicalImpact = impactWithCodon(MATCHING_CODON)
                    ),
                    TestVariantFactory.createMinimal().copy(
                        gene = TARGET_GENE,
                        isReportable = true,
                        clonalLikelihood = 0.3,
                        canonicalImpact = impactWithCodon(MATCHING_CODON)
                    )
                )
            )
        )
    }

    @Test
    fun `Should pass for a variant with matching canonical and non-canonical impact`() {
        assertMolecularEvaluation(
            EvaluationResult.PASS,
            function.evaluate(
                MolecularTestFactory.withDrivers(
                    TestVariantFactory.createMinimal().copy(
                        gene = TARGET_GENE,
                        isReportable = true,
                        canonicalImpact = impactWithCodon(MATCHING_CODON),
                        otherImpacts = setOf(impactWithCodon(MATCHING_CODON))
                    )
                )
            )
        )
    }

    @Test
    fun `Should evaluate undetermined with appropriate message when target coverage insufficient`() {
        val result = function.evaluate(
            TestPatientFactory.createMinimalTestWGSPatientRecord().copy(
                molecularTests = listOf(TestMolecularFactory.createMinimalPanelTest())
            )
        )
        assertThat(result.result).isEqualTo(EvaluationResult.UNDETERMINED)
        assertThat(result.undeterminedMessagesStrings())
            .containsExactly("Mutation in codons A100, B200 in gene gene A undetermined (not tested for mutations)")
    }

    private fun impactWithCodon(affectedCodon: Int) = TestTranscriptVariantImpactFactory.createMinimal().copy(affectedCodon = affectedCodon)
}