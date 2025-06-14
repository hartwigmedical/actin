package com.hartwig.actin.algo.evaluation.molecular

import com.hartwig.actin.algo.evaluation.EvaluationAssert
import com.hartwig.actin.datamodel.TestPatientFactory
import com.hartwig.actin.datamodel.algo.EvaluationResult
import com.hartwig.actin.datamodel.molecular.pharmaco.Haplotype
import com.hartwig.actin.datamodel.molecular.pharmaco.HaplotypeFunction
import com.hartwig.actin.datamodel.molecular.pharmaco.PharmacoEntry
import com.hartwig.actin.datamodel.molecular.pharmaco.PharmacoGene
import org.assertj.core.api.Assertions.assertThat
import org.junit.Test

class HasHomozygousDPYDDeficiencyTest {

    private val function = HasHomozygousDPYDDeficiency()

    @Test
    fun `Should return undetermined if patient has no DPYD pharmacology details`() {
        EvaluationAssert.assertMolecularEvaluation(
            EvaluationResult.UNDETERMINED,
            function.evaluate(
                MolecularTestFactory.withHaplotype(
                    PharmacoEntry(gene = PharmacoGene.UGT1A1, haplotypes = setOf(Haplotype(allele = "*1", alleleCount = 2, function = HaplotypeFunction.NORMAL_FUNCTION)))
                )
            )
        )
    }

    @Test
    fun `Should evaluate to undetermined when molecular record not available`() {
        val evaluation = function.evaluate(TestPatientFactory.createEmptyMolecularTestPatientRecord())
        assertThat(evaluation.result).isEqualTo(EvaluationResult.UNDETERMINED)
        assertThat(evaluation.undeterminedMessagesStrings()).containsExactly("No molecular data to determine homozygous DPYD deficiency")
    }

    @Test
    fun `Should pass if patient has homozygous DPYD haplotypes with reduced function`() {
        EvaluationAssert.assertMolecularEvaluation(
            EvaluationResult.PASS,
            function.evaluate(
                MolecularTestFactory.withHaplotype(
                    PharmacoEntry(gene = PharmacoGene.DPYD, haplotypes = setOf(Haplotype(allele = "*1", alleleCount = 2, function = HaplotypeFunction.REDUCED_FUNCTION)))
                )
            )
        )
    }

    @Test
    fun `Should pass if patient has heterozygous DPYD haplotypes with both no normal function`() {
        EvaluationAssert.assertMolecularEvaluation(
            EvaluationResult.PASS,
            function.evaluate(
                MolecularTestFactory.withHaplotype(
                    PharmacoEntry(
                        gene = PharmacoGene.DPYD,
                        haplotypes = setOf(
                            Haplotype(allele = "*1", alleleCount = 1, function = HaplotypeFunction.REDUCED_FUNCTION),
                            Haplotype(allele = "*2", alleleCount = 1, function = HaplotypeFunction.NO_FUNCTION)
                        )
                    )
                )
            )
        )
    }

    @Test
    fun `Should fail if patient has heterozygous DPYD haplotypes with at least one haplotype with normal function`() {
        EvaluationAssert.assertMolecularEvaluation(
            EvaluationResult.FAIL,
            function.evaluate(
                MolecularTestFactory.withHaplotype(
                    PharmacoEntry(
                        gene = PharmacoGene.DPYD,
                        haplotypes = setOf(
                            Haplotype(allele = "*1", alleleCount = 1, function = HaplotypeFunction.NORMAL_FUNCTION),
                            Haplotype(allele = "*2", alleleCount = 1, function = HaplotypeFunction.REDUCED_FUNCTION)
                        )
                    )
                )
            )
        )
    }

    @Test
    fun `Should fail if patient has homozygous DPYD haplotypes with normal function`() {
        EvaluationAssert.assertMolecularEvaluation(
            EvaluationResult.FAIL,
            function.evaluate(
                MolecularTestFactory.withHaplotype(
                    PharmacoEntry(gene = PharmacoGene.DPYD, haplotypes = setOf(Haplotype(allele = "*1", alleleCount = 2, function = HaplotypeFunction.NORMAL_FUNCTION)))
                )
            )
        )
    }
}