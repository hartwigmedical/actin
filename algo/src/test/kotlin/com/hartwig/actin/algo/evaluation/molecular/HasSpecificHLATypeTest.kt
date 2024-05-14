package com.hartwig.actin.algo.evaluation.molecular

import com.hartwig.actin.algo.datamodel.EvaluationResult
import com.hartwig.actin.algo.evaluation.EvaluationAssert.assertMolecularEvaluation
import com.hartwig.actin.molecular.datamodel.immunology.HlaAllele
import org.junit.Test

class HasSpecificHLATypeTest {

    @Test
    fun canEvaluate() {
        val correct = HlaAllele(name = "A*02:01", tumorCopyNumber = 1.0, hasSomaticMutations = false)
        val function = HasSpecificHLAType(correct.name)
        assertMolecularEvaluation(
            EvaluationResult.UNDETERMINED,
            function.evaluate(TestMolecularTestFactory.withUnreliableMolecularImmunology())
        )
        assertMolecularEvaluation(EvaluationResult.PASS, function.evaluate(TestMolecularTestFactory.withHlaAllele(correct)))
        assertMolecularEvaluation(
            EvaluationResult.WARN,
            function.evaluate(TestMolecularTestFactory.withHlaAllele(correct.copy(tumorCopyNumber = 0.0)))
        )
        assertMolecularEvaluation(
            EvaluationResult.WARN,
            function.evaluate(TestMolecularTestFactory.withHlaAllele(correct.copy(hasSomaticMutations = true)))
        )
        assertMolecularEvaluation(
            EvaluationResult.FAIL,
            function.evaluate(TestMolecularTestFactory.withHlaAllele(correct.copy(name = "other")))
        )
    }
}