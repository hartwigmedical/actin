package com.hartwig.actin.algo.evaluation.molecular

import com.hartwig.actin.algo.datamodel.EvaluationResult
import com.hartwig.actin.algo.evaluation.EvaluationAssert
import com.hartwig.actin.molecular.datamodel.hmf.pharmaco.Haplotype
import com.hartwig.actin.molecular.datamodel.hmf.pharmaco.PharmacoEntry
import org.junit.Test

class HasUGT1A1HaplotypeTest {

    private val function = HasUGT1A1Haplotype("*1_HET")

    @Test
    fun `Should pass if patient has at least one UGT1A1 allel with required haplotype`() {
        EvaluationAssert.assertMolecularEvaluation(
            EvaluationResult.PASS,
            function.evaluate(
                MolecularTestFactory.withHaplotype(
                    PharmacoEntry(
                        gene = "UGT1A1",
                        haplotypes = setOf(
                            Haplotype(name = "*1_HET", function = "Normal function"),
                            Haplotype(name = "*18_HET", function = "Normal function")
                        )
                    )
                )
            )
        )
    }

    @Test
    fun `Should fail if patient does not have required UGT1A1 haplotype`() {
        EvaluationAssert.assertMolecularEvaluation(
            EvaluationResult.FAIL,
            function.evaluate(
                MolecularTestFactory.withHaplotype(
                    PharmacoEntry(
                        gene = "UGT1A1",
                        haplotypes = setOf(
                            Haplotype(name = "*17_HET", function = "Normal function"),
                            Haplotype(name = "*18_HET", function = "Normal function")
                        )
                    )
                )
            )
        )
    }

    @Test
    fun `Should return undetermined if patient has no UGT1A1 haplotype information`() {
        EvaluationAssert.assertMolecularEvaluation(
            EvaluationResult.UNDETERMINED,
            function.evaluate(
                MolecularTestFactory.withHaplotype(
                    PharmacoEntry(
                        gene = "DPYD",
                        haplotypes = setOf(Haplotype(name = "*1_HOM", function = "Normal function")),
                    )
                )
            )
        )
    }
}