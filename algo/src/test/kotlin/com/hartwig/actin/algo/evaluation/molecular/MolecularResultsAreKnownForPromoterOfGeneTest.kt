package com.hartwig.actin.algo.evaluation.molecular

import com.hartwig.actin.algo.evaluation.EvaluationAssert.assertEvaluation
import com.hartwig.actin.datamodel.algo.EvaluationResult
import com.hartwig.actin.datamodel.clinical.PriorIHCTest
import org.junit.Test

class MolecularResultsAreKnownForPromoterOfGeneTest {

    @Test
    fun canEvaluate() {
        val function = MolecularResultsAreKnownForPromoterOfGene("gene 1")
        assertEvaluation(EvaluationResult.PASS, function.evaluate(MolecularTestFactory.withIHCTests(create("gene 1 promoter", false))))
        assertEvaluation(
            EvaluationResult.UNDETERMINED,
            function.evaluate(MolecularTestFactory.withIHCTests(create("gene 1 promoter", true)))
        )
        assertEvaluation(EvaluationResult.FAIL, function.evaluate(MolecularTestFactory.withIHCTests(create("gene 1 coding", false))))
        assertEvaluation(EvaluationResult.FAIL, function.evaluate(MolecularTestFactory.withIHCTests(create("gene 2 promoter", false))))
    }

    private fun create(gene: String, impliesPotentialDeterminateStatus: Boolean): PriorIHCTest {
        return MolecularTestFactory.priorIHCTest(
            test = "IHC",
            item = gene,
            impliesIndeterminate = impliesPotentialDeterminateStatus
        )
    }
}