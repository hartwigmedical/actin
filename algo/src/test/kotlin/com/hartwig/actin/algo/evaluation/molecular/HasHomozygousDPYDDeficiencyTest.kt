package com.hartwig.actin.algo.evaluation.molecular

import com.hartwig.actin.algo.datamodel.EvaluationResult
import com.hartwig.actin.algo.evaluation.EvaluationAssert
import com.hartwig.actin.molecular.datamodel.orange.pharmaco.Haplotype
import com.hartwig.actin.molecular.datamodel.orange.pharmaco.PharmacoEntry
import org.junit.Test

class HasHomozygousDPYDDeficiencyTest {

    private val function = HasHomozygousDPYDDeficiency()

    @Test
    fun `Should return undetermined if patient has no DPYD pharmacology details`() {
        EvaluationAssert.assertMolecularEvaluation(
            EvaluationResult.UNDETERMINED,
            function.evaluate(
                MolecularTestFactory.withHaplotype(
                    PharmacoEntry(gene = "UGT1A1", haplotypes = setOf(Haplotype(allele = "*1", alleleCount = 2, function = "Normal Function")))
                )
            )
        )
    }

    @Test
    fun `Should pass if patient has homozygous DPYD haplotypes with reduced function`() {
        EvaluationAssert.assertMolecularEvaluation(
            EvaluationResult.PASS,
            function.evaluate(
                MolecularTestFactory.withHaplotype(
                    PharmacoEntry(gene = "DPYD", haplotypes = setOf(Haplotype(allele = "*1", alleleCount = 2, function = "Reduced Function")))
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
                        gene = "DPYD",
                        haplotypes = setOf(
                            Haplotype(allele = "*1", alleleCount = 1, function = "Reduced Function"),
                            Haplotype(allele = "*2", alleleCount = 1, function = "No Function")
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
                        gene = "DPYD",
                        haplotypes = setOf(
                            Haplotype(allele = "*1", alleleCount = 1, function = "Normal Function"),
                            Haplotype(allele = "*2", alleleCount = 1, function = "Reduced Function")
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
                    PharmacoEntry(gene = "DPYD", haplotypes = setOf(Haplotype(allele = "*1", alleleCount = 2, function = "Normal Function")))
                )
            )
        )
    }
}