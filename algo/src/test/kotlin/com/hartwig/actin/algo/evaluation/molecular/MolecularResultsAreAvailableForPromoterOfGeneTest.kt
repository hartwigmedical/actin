package com.hartwig.actin.algo.evaluation.molecular

import com.hartwig.actin.clinical.datamodel.PriorIHCTest
import org.junit.Test

class MolecularResultsAreAvailableForPromoterOfGeneTest {

    @Test
    fun canEvaluate() {
      /*  val function = MolecularResultsAreAvailableForPromoterOfGene("gene 1")
        assertEvaluation(EvaluationResult.PASS, function.evaluate(MolecularTestFactory.withMolecularTest(create("gene 1 promoter", false))))
        assertEvaluation(
            EvaluationResult.UNDETERMINED,
            function.evaluate(MolecularTestFactory.withMolecularTest(create("gene 1 promoter", true)))
        )
        assertEvaluation(EvaluationResult.FAIL, function.evaluate(MolecularTestFactory.withMolecularTest(create("gene 1 coding", false))))
        assertEvaluation(EvaluationResult.FAIL, function.evaluate(MolecularTestFactory.withMolecularTest(create("gene 2 promoter", false))))*/
    }

    private fun create(gene: String, impliesPotentialDeterminateStatus: Boolean): PriorIHCTest {
      return MolecularTestFactory.priorMolecularTest(
            test = "IHC",
            item = gene,
            impliesIndeterminate = impliesPotentialDeterminateStatus
        )
    }
}