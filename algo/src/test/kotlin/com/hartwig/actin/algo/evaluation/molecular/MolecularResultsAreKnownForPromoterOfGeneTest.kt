package com.hartwig.actin.algo.evaluation.molecular

import com.hartwig.actin.algo.evaluation.EvaluationAssert.assertEvaluation
import com.hartwig.actin.datamodel.algo.EvaluationResult
import com.hartwig.actin.datamodel.clinical.IhcTest
import org.junit.Test

class MolecularResultsAreKnownForPromoterOfGeneTest {

    private val function = MolecularResultsAreKnownForPromoterOfGene("gene 1")

    @Test
    fun `Should pass if promoter of gene tested by IHC with determinate results`() {
        assertEvaluation(EvaluationResult.PASS, function.evaluate(MolecularTestFactory.withIhcTests(create("gene 1 promoter", false))))
    }

    @Test
    fun `Should warn if promoter of gene tested by IHC with indeterminate results`() {
        assertEvaluation(
            EvaluationResult.WARN,
            function.evaluate(MolecularTestFactory.withIhcTests(create("gene 1 promoter", true)))
        )
    }

    @Test
    fun `Should be undetermined if promoter of gene not tested by IHC`() {
        assertEvaluation(EvaluationResult.UNDETERMINED, function.evaluate(MolecularTestFactory.withIhcTests(create("gene 1 coding", false))))
        assertEvaluation(EvaluationResult.UNDETERMINED, function.evaluate(MolecularTestFactory.withIhcTests(create("gene 2 promoter", false))))
    }

    private fun create(gene: String, impliesPotentialDeterminateStatus: Boolean): IhcTest {
        return MolecularTestFactory.ihcTest(
            item = gene,
            impliesIndeterminate = impliesPotentialDeterminateStatus
        )
    }
}