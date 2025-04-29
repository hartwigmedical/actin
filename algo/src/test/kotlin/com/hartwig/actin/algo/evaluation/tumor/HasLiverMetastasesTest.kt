package com.hartwig.actin.algo.evaluation.tumor

import com.hartwig.actin.algo.evaluation.EvaluationAssert.assertEvaluation
import com.hartwig.actin.datamodel.algo.EvaluationResult
import com.hartwig.actin.doid.TestDoidModelFactory
import org.junit.Test

class HasLiverMetastasesTest {
    private val function: HasLiverMetastases = HasLiverMetastases(TestDoidModelFactory.createMinimalTestDoidModel())

    @Test
    fun shouldBeUndeterminedWhenHasLiverLesionsIsNull() {
        val undetermined = function.evaluate(TumorTestFactory.withLiverLesions(null))
        assertEvaluation(EvaluationResult.UNDETERMINED, undetermined)
    }

    @Test
    fun shouldPassWhenHasLiverLesionsIsTrue() {
        val pass = function.evaluate(TumorTestFactory.withLiverLesions(true))
        assertEvaluation(EvaluationResult.PASS, pass)
    }

    @Test
    fun shouldFailWhenHasLiverLesionsIsFalse() {
        val fail = function.evaluate(TumorTestFactory.withLiverLesions(false))
        assertEvaluation(EvaluationResult.FAIL, fail)
    }
}