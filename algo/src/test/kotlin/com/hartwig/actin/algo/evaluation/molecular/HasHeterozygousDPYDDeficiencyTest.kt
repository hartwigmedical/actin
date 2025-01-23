package com.hartwig.actin.algo.evaluation.molecular

import com.hartwig.actin.algo.evaluation.EvaluationAssert
import com.hartwig.actin.datamodel.algo.EvaluationResult
import com.hartwig.actin.datamodel.molecular.pharmaco.Haplotype
import com.hartwig.actin.datamodel.molecular.pharmaco.HaplotypeFunction
import com.hartwig.actin.datamodel.molecular.pharmaco.PharmacoEntry
import com.hartwig.actin.datamodel.molecular.pharmaco.PharmacoGene
import org.junit.Test

class HasHeterozygousDPYDDeficiencyTest {

    private val function = HasHeterozygousDPYDDeficiency()

    @Test
    fun `Should return undetermined if patient has no DPYD pharmacology details`() {
        EvaluationAssert.assertMolecularEvaluation(
            EvaluationResult.UNDETERMINED,
            function.evaluate(
                MolecularTestFactory.withHaplotype(
                    PharmacoEntry(
                        gene = PharmacoGene.UGT1A1,
                        haplotypes = setOf(Haplotype(allele = "*1", alleleCount = 2, function = HaplotypeFunction.NORMAL_FUNCTION))
                    )
                )
            )
        )
    }

    @Test
    fun `Should pass if patient has heterozygous DPYD haplotypes of which one has reduced function`() {
        EvaluationAssert.assertMolecularEvaluation(
            EvaluationResult.PASS,
            function.evaluate(
                MolecularTestFactory.withHaplotype(
                    PharmacoEntry(
                        gene = PharmacoGene.DPYD,
                        haplotypes = setOf(
                            Haplotype(allele = "*1", alleleCount = 1, function = HaplotypeFunction.REDUCED_FUNCTION),
                            Haplotype(allele = "*2", alleleCount = 1, function = HaplotypeFunction.NORMAL_FUNCTION)
                        )
                    )
                )
            )
        )
    }

    @Test
    fun `Should pass if patient has heterozygous DPYD haplotypes of which one has no function`() {
        EvaluationAssert.assertMolecularEvaluation(
            EvaluationResult.PASS,
            function.evaluate(
                MolecularTestFactory.withHaplotype(
                    PharmacoEntry(
                        gene = PharmacoGene.DPYD,
                        haplotypes = setOf(
                            Haplotype(allele = "*1", alleleCount = 1, function = HaplotypeFunction.NO_FUNCTION),
                            Haplotype(allele = "*2", alleleCount = 1, function = HaplotypeFunction.NORMAL_FUNCTION)
                        )
                    )
                )
            )
        )
    }

    @Test
    fun `Should fail if patient has homozygous DPYD haplotypes with reduced function`() {
        EvaluationAssert.assertMolecularEvaluation(
            EvaluationResult.FAIL,
            function.evaluate(
                MolecularTestFactory.withHaplotype(
                    PharmacoEntry(
                        gene = PharmacoGene.DPYD,
                        haplotypes = setOf(
                            Haplotype(allele = "*1", alleleCount = 2, function = HaplotypeFunction.REDUCED_FUNCTION)
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
                    PharmacoEntry(
                        gene = PharmacoGene.DPYD,
                        haplotypes = setOf(Haplotype(allele = "*1", alleleCount = 2, function = HaplotypeFunction.NORMAL_FUNCTION))
                    )
                )
            )
        )
    }

    @Test
    fun `Should fail if patient has heterozygous DPYD haplotypes both with reduced function`() {
        EvaluationAssert.assertMolecularEvaluation(
            EvaluationResult.FAIL,
            function.evaluate(
                MolecularTestFactory.withHaplotype(
                    PharmacoEntry(
                        gene = PharmacoGene.DPYD,
                        haplotypes = setOf(
                            Haplotype(allele = "*1", alleleCount = 1, function = HaplotypeFunction.REDUCED_FUNCTION),
                            Haplotype(allele = "*2", alleleCount = 1, function = HaplotypeFunction.REDUCED_FUNCTION)
                        )
                    )
                )
            )
        )
    }

    @Test
    fun `Should fail if patient has heterozygous DPYD haplotypes both with both normal function`() {
        EvaluationAssert.assertMolecularEvaluation(
            EvaluationResult.FAIL,
            function.evaluate(
                MolecularTestFactory.withHaplotype(
                    PharmacoEntry(
                        gene = PharmacoGene.DPYD,
                        haplotypes = setOf(
                            Haplotype(allele = "*1", alleleCount = 1, function = HaplotypeFunction.NORMAL_FUNCTION),
                            Haplotype(allele = "*2", alleleCount = 1, function = HaplotypeFunction.NORMAL_FUNCTION)
                        )
                    )
                )
            )
        )
    }
}