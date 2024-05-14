package com.hartwig.actin.algo.evaluation.tumor

import com.hartwig.actin.algo.datamodel.EvaluationResult
import com.hartwig.actin.algo.evaluation.EvaluationAssert.assertEvaluation
import org.junit.Test

class HasBoneMetastasesTest {
    private val function: HasBoneMetastases = HasBoneMetastases()

    @Test
    fun shouldBeUndeterminedWhenHasBoneLesionsIsNull() {
        val undetermined = function.evaluate(TestTumorFactory.withBoneLesions(null))
        assertEvaluation(EvaluationResult.UNDETERMINED, undetermined)
    }

    @Test
    fun shouldPassWhenHasBoneLesionsIsTrue() {
        val pass = function.evaluate(TestTumorFactory.withBoneLesions(true))
        assertEvaluation(EvaluationResult.PASS, pass)
    }

    @Test
    fun shouldFailWhenHasBoneLesionsIsFalse() {
        val fail = function.evaluate(TestTumorFactory.withBoneLesions(false))
        assertEvaluation(EvaluationResult.FAIL, fail)
    }
}