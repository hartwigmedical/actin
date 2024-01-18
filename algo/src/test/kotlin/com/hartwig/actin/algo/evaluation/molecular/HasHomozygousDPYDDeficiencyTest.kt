package com.hartwig.actin.algo.evaluation.molecular

import com.hartwig.actin.algo.datamodel.EvaluationResult
import com.hartwig.actin.algo.evaluation.EvaluationAssert
import com.hartwig.actin.molecular.datamodel.pharmaco.ImmutablePharmacoEntry
import com.hartwig.actin.molecular.datamodel.pharmaco.TestPharmacoFactory
import org.junit.Test

class HasHomozygousDPYDDeficiencyTest {

    private val function = HasHomozygousDPYDDeficiency()

    @Test
    fun `Should pass if patient has homozygous DPYD haplotypes with reduced function`() {
        EvaluationAssert.assertMolecularEvaluation(
            EvaluationResult.PASS,
            function.evaluate(
                MolecularTestFactory.withHaplotype(
                    ImmutablePharmacoEntry.builder()
                        .gene("DPYD")
                        .addHaplotypes(TestPharmacoFactory.builder().name("*1_HOM").function("Reduced function").build())
                        .build()
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
                    ImmutablePharmacoEntry.builder()
                        .gene("DPYD")
                        .addHaplotypes(TestPharmacoFactory.builder().name("*1_HET").function("Reduced function").build())
                        .addHaplotypes(TestPharmacoFactory.builder().name("*1_HET").function("Reduced function").build())
                        .build()
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
                    ImmutablePharmacoEntry.builder()
                        .gene("DPYD")
                        .addHaplotypes(TestPharmacoFactory.builder().name("*1_HET").function("Normal function").build())
                        .addHaplotypes(TestPharmacoFactory.builder().name("*1_HET").function("Reduced function").build())
                        .build()
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
                    ImmutablePharmacoEntry.builder()
                        .gene("DPYD")
                        .addHaplotypes(TestPharmacoFactory.builder().name("*1_HOM").function("Normal function").build())
                        .build()
                )
            )
        )
    }

    @Test
    fun `Should return undetermined if patient has no DPYD type information`() {
        EvaluationAssert.assertMolecularEvaluation(
            EvaluationResult.UNDETERMINED,
            function.evaluate(
                MolecularTestFactory.withHaplotype(
                    ImmutablePharmacoEntry.builder()
                        .gene("UGT1A1")
                        .addHaplotypes(TestPharmacoFactory.builder().name("*1_HOM").function("Normal function").build())
                        .build()
                )
            )
        )
    }

}