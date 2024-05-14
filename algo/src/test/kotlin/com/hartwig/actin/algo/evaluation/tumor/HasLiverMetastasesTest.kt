package com.hartwig.actin.algo.evaluation.tumor

import com.hartwig.actin.algo.datamodel.EvaluationResult
import com.hartwig.actin.algo.evaluation.EvaluationAssert.assertEvaluation
import org.junit.Test

class HasLiverMetastasesTest {
    private val function: HasLiverMetastases = HasLiverMetastases()

    @Test
    fun shouldBeUndeterminedWhenHasLiverLesionsIsNull() {
        val undetermined = function.evaluate(TestTumorFactory.withLiverLesions(null))
        assertEvaluation(EvaluationResult.UNDETERMINED, undetermined)
    }

    @Test
    fun shouldPassWhenHasLiverLesionsIsTrue() {
        val pass = function.evaluate(TestTumorFactory.withLiverLesions(true))
        assertEvaluation(EvaluationResult.PASS, pass)
    }

    @Test
    fun shouldFailWhenHasLiverLesionsIsFalse() {
        val fail = function.evaluate(TestTumorFactory.withLiverLesions(false))
        assertEvaluation(EvaluationResult.FAIL, fail)
    }
}