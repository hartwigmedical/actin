package com.hartwig.actin.algo.evaluation.tumor

import com.hartwig.actin.algo.datamodel.EvaluationResult
import com.hartwig.actin.algo.evaluation.EvaluationAssert.assertEvaluation
import org.junit.Test

class HasLungMetastasesTest {
    private val function: HasLungMetastases = HasLungMetastases()

    @Test
    fun shouldBeUndeterminedWhenHasLungLesionsIsNull() {
        val undetermined = function.evaluate(TumorTestFactory.withLungLesions(null))
        assertEvaluation(EvaluationResult.UNDETERMINED, undetermined)
    }

    @Test
    fun shouldPassWhenHasLungLesionsIsTrue() {
        val pass = function.evaluate(TumorTestFactory.withLungLesions(true))
        assertEvaluation(EvaluationResult.PASS, pass)
    }

    @Test
    fun shouldFailWhenHasLungLesionsIsFalse() {
        val fail = function.evaluate(TumorTestFactory.withLungLesions(false))
        assertEvaluation(EvaluationResult.FAIL, fail)
    }
}