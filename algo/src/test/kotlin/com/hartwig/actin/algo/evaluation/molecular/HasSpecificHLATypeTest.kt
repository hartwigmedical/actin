package com.hartwig.actin.algo.evaluation.molecular

import com.hartwig.actin.algo.datamodel.EvaluationResult
import com.hartwig.actin.algo.evaluation.EvaluationAssert.assertMolecularEvaluation
import com.hartwig.actin.molecular.datamodel.immunology.HlaAllele
import com.hartwig.actin.molecular.datamodel.immunology.ImmutableHlaAllele
import com.hartwig.actin.molecular.datamodel.immunology.TestHlaAlleleFactory
import org.junit.Test

class HasSpecificHLATypeTest {
    @Test
    fun canEvaluate() {
        val correct: HlaAllele = ImmutableHlaAllele.builder().name("A*02:01").tumorCopyNumber(1.0).hasSomaticMutations(false).build()
        val function = HasSpecificHLAType(correct.name())
        assertMolecularEvaluation(
            EvaluationResult.UNDETERMINED,
            function.evaluate(MolecularTestFactory.withUnreliableMolecularImmunology())
        )
        assertMolecularEvaluation(EvaluationResult.PASS, function.evaluate(MolecularTestFactory.withHlaAllele(correct)))
        assertMolecularEvaluation(
            EvaluationResult.WARN,
            function.evaluate(
                MolecularTestFactory.withHlaAllele(
                    TestHlaAlleleFactory.builder()
                        .from(correct)
                        .tumorCopyNumber(0.0)
                        .build()
                )
            )
        )
        assertMolecularEvaluation(
            EvaluationResult.WARN,
            function.evaluate(
                MolecularTestFactory.withHlaAllele(
                    TestHlaAlleleFactory.builder()
                        .from(correct)
                        .hasSomaticMutations(true)
                        .build()
                )
            )
        )
        assertMolecularEvaluation(
            EvaluationResult.FAIL,
            function.evaluate(MolecularTestFactory.withHlaAllele(TestHlaAlleleFactory.builder().from(correct).name("other").build()))
        )
    }
}