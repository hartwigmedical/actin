package com.hartwig.actin.algo.evaluation.molecular

import com.hartwig.actin.algo.datamodel.EvaluationResult
import com.hartwig.actin.algo.evaluation.EvaluationAssert
import com.hartwig.actin.molecular.datamodel.orange.pharmaco.Haplotype
import com.hartwig.actin.molecular.datamodel.orange.pharmaco.PharmacoEntry
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
                            Haplotype(allele = "*1", alleleCount = 1, function = "Normal function"),
                            Haplotype(allele = "*18", alleleCount = 1, function = "Normal function")
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
                            Haplotype(allele = "*17", alleleCount = 1, function = "Normal function"),
                            Haplotype(allele = "*18", alleleCount = 1, function = "Normal function")
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
                        haplotypes = setOf(Haplotype(allele = "*1", alleleCount = 2, function = "Normal function")),
                    )
                )
            )
        )
    }
}