package com.hartwig.actin.algo.evaluation.molecular

import com.hartwig.actin.algo.datamodel.EvaluationResult
import com.hartwig.actin.algo.evaluation.EvaluationAssert
import com.hartwig.actin.molecular.datamodel.wgs.pharmaco.Haplotype
import com.hartwig.actin.molecular.datamodel.wgs.pharmaco.PharmacoEntry
import org.junit.Test

class HasHomozygousDPYDDeficiencyTest {

    private val function = HasHomozygousDPYDDeficiency()

    @Test
    fun `Should return undetermined if patient has no DPYD pharmacology details`() {
        EvaluationAssert.assertMolecularEvaluation(
            EvaluationResult.UNDETERMINED,
            function.evaluate(
                MolecularTestFactory.withHaplotype(
                    PharmacoEntry(gene = "UGT1A1", haplotypes = setOf(Haplotype(name = "*1_HOM", function = "Normal Function")))
                )
            )
        )
    }

    @Test
    fun `Should return undetermined if patient has unexpected DPYD type function`() {
        EvaluationAssert.assertMolecularEvaluation(
            EvaluationResult.UNDETERMINED,
            function.evaluate(
                MolecularTestFactory.withHaplotype(
                    PharmacoEntry(
                        gene = "DPYD",
                        haplotypes = setOf(
                            Haplotype(name = "*1_HET", function = "Normal Function"),
                            Haplotype(name = "*1_HET", function = "Unexpected Function")
                        )
                    )
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
                    PharmacoEntry(gene = "DPYD", haplotypes = setOf(Haplotype(name = "*1_HOM", function = "Reduced Function")))
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
                            Haplotype(name = "*1_HET", function = "Reduced Function"),
                            Haplotype(name = "*1_HET", function = "No Function")
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
                            Haplotype(name = "*1_HET", function = "Normal Function"),
                            Haplotype(name = "*1_HET", function = "Reduced Function")
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
                    PharmacoEntry(gene = "DPYD", haplotypes = setOf(Haplotype(name = "*1_HOM", function = "Normal Function")))
                )
            )
        )
    }
}