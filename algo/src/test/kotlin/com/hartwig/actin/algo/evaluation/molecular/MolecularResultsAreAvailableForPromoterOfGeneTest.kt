package com.hartwig.actin.algo.evaluation.molecular

import com.hartwig.actin.algo.datamodel.EvaluationResult
import com.hartwig.actin.algo.evaluation.EvaluationAssert.assertEvaluation
import com.hartwig.actin.molecular.datamodel.IHCMolecularTest
import com.hartwig.actin.molecular.datamodel.MolecularTest
import org.junit.Test

class MolecularResultsAreAvailableForPromoterOfGeneTest {

    @Test
    fun canEvaluate() {
        val function = MolecularResultsAreAvailableForPromoterOfGene("gene 1")
        assertEvaluation(EvaluationResult.PASS, function.evaluate(MolecularTestFactory.withPriorTest(create("gene 1 promoter", false))))
        assertEvaluation(
            EvaluationResult.UNDETERMINED,
            function.evaluate(MolecularTestFactory.withPriorTest(create("gene 1 promoter", true)))
        )
        assertEvaluation(EvaluationResult.FAIL, function.evaluate(MolecularTestFactory.withPriorTest(create("gene 1 coding", false))))
        assertEvaluation(EvaluationResult.FAIL, function.evaluate(MolecularTestFactory.withPriorTest(create("gene 2 promoter", false))))
    }

    private fun create(gene: String, impliesPotentialDeterminateStatus: Boolean): MolecularTest {
        return IHCMolecularTest(
            MolecularTestFactory.priorMolecularTest(
                test = "IHC",
                item = gene,
                impliesIndeterminate = impliesPotentialDeterminateStatus
            )
        )
    }
}