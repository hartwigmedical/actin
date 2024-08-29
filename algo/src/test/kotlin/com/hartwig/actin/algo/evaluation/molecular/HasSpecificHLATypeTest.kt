package com.hartwig.actin.algo.evaluation.molecular

import com.hartwig.actin.algo.evaluation.EvaluationAssert.assertMolecularEvaluation
import com.hartwig.actin.datamodel.algo.EvaluationResult
import com.hartwig.actin.datamodel.molecular.orange.immunology.HlaAllele
import org.junit.Test

class HasSpecificHLATypeTest {

    @Test
    fun canEvaluate() {
        val correct = HlaAllele(name = "A*02:01", tumorCopyNumber = 1.0, hasSomaticMutations = false)
        val function = HasSpecificHLAType(correct.name)
        assertMolecularEvaluation(
            EvaluationResult.UNDETERMINED,
            function.evaluate(MolecularTestFactory.withUnreliableMolecularImmunology())
        )
        assertMolecularEvaluation(EvaluationResult.PASS, function.evaluate(MolecularTestFactory.withHlaAllele(correct)))
        assertMolecularEvaluation(
            EvaluationResult.WARN,
            function.evaluate(MolecularTestFactory.withHlaAllele(correct.copy(tumorCopyNumber = 0.0)))
        )
        assertMolecularEvaluation(
            EvaluationResult.WARN,
            function.evaluate(MolecularTestFactory.withHlaAllele(correct.copy(hasSomaticMutations = true)))
        )
        assertMolecularEvaluation(
            EvaluationResult.FAIL,
            function.evaluate(MolecularTestFactory.withHlaAllele(correct.copy(name = "other")))
        )
    }
}