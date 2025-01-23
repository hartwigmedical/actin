package com.hartwig.actin.algo.evaluation.molecular

import com.hartwig.actin.algo.evaluation.EvaluationAssert
import com.hartwig.actin.datamodel.algo.EvaluationResult
import com.hartwig.actin.datamodel.molecular.pharmaco.Haplotype
import com.hartwig.actin.datamodel.molecular.pharmaco.HaplotypeFunction
import com.hartwig.actin.datamodel.molecular.pharmaco.PharmacoEntry
import com.hartwig.actin.datamodel.molecular.pharmaco.PharmacoGene
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
                        gene = PharmacoGene.UGT1A1,
                        haplotypes = setOf(
                            Haplotype(allele = "*1", alleleCount = 1, function = HaplotypeFunction.NORMAL_FUNCTION),
                            Haplotype(allele = "*18", alleleCount = 1, function = HaplotypeFunction.NORMAL_FUNCTION)
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
                        gene = PharmacoGene.UGT1A1,
                        haplotypes = setOf(
                            Haplotype(allele = "*17", alleleCount = 1, function = HaplotypeFunction.NORMAL_FUNCTION),
                            Haplotype(allele = "*18", alleleCount = 1, function = HaplotypeFunction.NORMAL_FUNCTION)
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
                        gene = PharmacoGene.DPYD,
                        haplotypes = setOf(Haplotype(allele = "*1", alleleCount = 2, function = HaplotypeFunction.NORMAL_FUNCTION)),
                    )
                )
            )
        )
    }
}