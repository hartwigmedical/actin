package com.hartwig.actin.algo.evaluation.molecular

import com.hartwig.actin.algo.datamodel.EvaluationResult
import com.hartwig.actin.algo.evaluation.EvaluationAssert
import com.hartwig.actin.molecular.datamodel.pharmaco.ImmutableHaplotype
import com.hartwig.actin.molecular.datamodel.pharmaco.ImmutablePharmacoEntry
import org.apache.logging.log4j.util.Strings
import org.junit.Test

class HasSpecificUGT1A1TypeTest {

    @Test
    fun `Should pass if patient has required UGT1A1 type`() {
        EvaluationAssert.assertMolecularEvaluation(
            EvaluationResult.PASS,
            FUNCTION.evaluate(
                MolecularTestFactory.withUGT1A1(
                    createPharmacoEntryWithHaplotype("UGT1A1", "*1_HOM")
                )
            )
        )
    }

    @Test
    fun `Should fail if patient does not have required UGT1A1 type`() {
        EvaluationAssert.assertMolecularEvaluation(
            EvaluationResult.FAIL,
            FUNCTION.evaluate(
                MolecularTestFactory.withUGT1A1(
                    createPharmacoEntryWithHaplotype("UGT1A1", "*28_HOM")
                )
            )
        )
    }

    @Test
    fun `Should return undetermined if patient has no UGT1A1 type information`() {
        EvaluationAssert.assertMolecularEvaluation(
            EvaluationResult.UNDETERMINED,
            FUNCTION.evaluate(
                MolecularTestFactory.withUGT1A1(
                    createPharmacoEntryWithHaplotype("DPYD", "*28_HOM")
                )
            )
        )
    }

    companion object {
        val FUNCTION = HasSpecificUGT1A1Type("*1_HOM")

        private fun createPharmacoEntryWithHaplotype(gene: String, haplotype: String): ImmutablePharmacoEntry {
            return ImmutablePharmacoEntry.builder()
                .gene(gene)
                .haplotypes(setOf(ImmutableHaplotype.builder().name(haplotype).function(Strings.EMPTY).build())).build()
        }
    }

}