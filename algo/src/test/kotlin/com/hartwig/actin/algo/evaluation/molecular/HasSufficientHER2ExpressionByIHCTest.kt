package com.hartwig.actin.algo.evaluation.molecular

import com.hartwig.actin.algo.datamodel.EvaluationResult
import com.hartwig.actin.algo.evaluation.EvaluationAssert.assertEvaluation
import com.hartwig.actin.algo.evaluation.util.ValueComparison
import com.hartwig.actin.molecular.datamodel.IHCMolecularTest
import org.junit.Test

class HasSufficientHER2ExpressionByIHCTest {
    private val function = HasSufficientHER2ExpressionByIHC(2.0)

    @Test
    fun `Should fail when there are no prior tests`() {
        assertEvaluation(EvaluationResult.FAIL, function.evaluate(MolecularTestFactory.withMolecularTests(emptyList())))
    }

    @Test
    fun `Should fail when no prior test contains results`() {
        assertEvaluation(EvaluationResult.FAIL, function.evaluate(MolecularTestFactory.withMolecularTest(ihcTest())))
    }

    @Test
    fun `Should fail if prior test contains result that is too low`() {
        assertEvaluation(EvaluationResult.FAIL, function.evaluate(MolecularTestFactory.withMolecularTest(ihcTest(scoreValue = 1.0))))
    }

    @Test
    fun `Should pass when prior test contains result that is over the threshold`() {
        assertEvaluation(EvaluationResult.PASS, function.evaluate(MolecularTestFactory.withMolecularTest(ihcTest(scoreValue = 3.0))))
    }

    @Test
    fun `Should fail when prior test contains exact result with prefix`(){
        val priorTest = ihcTest(scoreValuePrefix = ValueComparison.LARGER_THAN, scoreValue = 2.0)
        assertEvaluation(EvaluationResult.FAIL, function.evaluate(MolecularTestFactory.withMolecularTest(priorTest)))
    }

    @Test
    fun `Should fail when test value has non-matching prefix`(){
        val priorTest = ihcTest(scoreValuePrefix = ValueComparison.SMALLER_THAN, scoreValue = 3.0)
        assertEvaluation(EvaluationResult.FAIL, function.evaluate(MolecularTestFactory.withMolecularTest(priorTest)))
    }

    @Test
    fun `Should fail if only positive or negative result available`() {
        assertEvaluation(EvaluationResult.FAIL,
            function.evaluate(
                MolecularTestFactory.withMolecularTests(
                    listOf(ihcTest(scoreText = "negative"), ihcTest(scoreText = "undetermined"))
                )
            )
        )
    }

    @Test
    fun `Should resolve to undetermined if the impliesIndeterminate status variable is set`() {
        assertEvaluation(EvaluationResult.UNDETERMINED,
            function.evaluate(
                MolecularTestFactory.withMolecularTest(ihcTest(scoreValue = 3.0, impliesIndeterminate = true))
            ))
    }

    private fun ihcTest(scoreValue: Double? = null, scoreValuePrefix: String? = null, scoreText: String? = null, impliesIndeterminate: Boolean = false): IHCMolecularTest {
        return IHCMolecularTest(
            MolecularTestFactory.priorMolecularTest(
                test = "IHC", item = "HER2", scoreValue = scoreValue, scoreValuePrefix = scoreValuePrefix, scoreText = scoreText, impliesIndeterminate = impliesIndeterminate
            )
        )
    }

}