package com.hartwig.actin.algo.evaluation.molecular

import com.hartwig.actin.algo.datamodel.EvaluationResult
import com.hartwig.actin.algo.evaluation.EvaluationAssert
import com.hartwig.actin.molecular.datamodel.pharmaco.ImmutablePharmacoEntry
import com.hartwig.actin.molecular.datamodel.pharmaco.TestPharmacoFactory
import org.junit.Test

class HasAtLeastUGT1A1HaplotypeTest {

    @Test
    fun `Should pass if patient has at least one UGT1A1 allel with required haplotype`() {
        val function = HasAtLeastUGT1A1Haplotype("*1_HET")

        EvaluationAssert.assertMolecularEvaluation(
            EvaluationResult.PASS,
            function.evaluate(
                MolecularTestFactory.withUGT1A1(
                    ImmutablePharmacoEntry.builder()
                        .gene("UGT1A1")
                        .addHaplotypes(TestPharmacoFactory.builder().name("*1_HET").function("Normal function").build())
                        .addHaplotypes(TestPharmacoFactory.builder().name("*18_HET").function("Normal function").build())
                        .build()
                )
            )
        )
    }

    @Test
    fun `Should fail if patient does not have required UGT1A1 haplotype`() {
        val function = HasAtLeastUGT1A1Haplotype("*1_HOM")
        EvaluationAssert.assertMolecularEvaluation(
            EvaluationResult.FAIL,
            function.evaluate(
                MolecularTestFactory.withUGT1A1(
                    ImmutablePharmacoEntry.builder()
                        .gene("UGT1A1")
                        .addHaplotypes(TestPharmacoFactory.builder().name("*1_HET").function("Normal function").build())
                        .addHaplotypes(TestPharmacoFactory.builder().name("*18_HET").function("Normal function").build())
                        .build()
                )
            )
        )
    }

    @Test
    fun `Should return undetermined if patient has no UGT1A1 haplotype information`() {
        val function = HasAtLeastUGT1A1Haplotype("*1_HOM")
        EvaluationAssert.assertMolecularEvaluation(
            EvaluationResult.UNDETERMINED,
            function.evaluate(
                MolecularTestFactory.withUGT1A1(
                    ImmutablePharmacoEntry.builder()
                        .gene("DPYD")
                        .addHaplotypes(TestPharmacoFactory.builder().name("*1_HOM").function("Normal function").build())
                        .build()
                )
            )
        )
    }

}