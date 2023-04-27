package com.hartwig.actin.algo.evaluation.tumor

import com.hartwig.actin.algo.datamodel.EvaluationResult
import com.hartwig.actin.algo.evaluation.EvaluationAssert.assertEvaluation
import org.junit.Test

class HasBoneMetastasesTest {
    private val function: HasBoneMetastases = HasBoneMetastases()

    @Test
    fun shouldBeUndeterminedWhenHasBoneLesionsIsNull() {
        val undetermined = function.evaluate(TumorTestFactory.withBoneLesions(null))
        assertEvaluation(EvaluationResult.UNDETERMINED, undetermined)
    }

    @Test
    fun shouldPassWhenHasBoneLesionsIsTrue() {
        val pass = function.evaluate(TumorTestFactory.withBoneLesions(true))
        assertEvaluation(EvaluationResult.PASS, pass)
    }

    @Test
    fun shouldFailWhenHasBoneLesionsIsFalse() {
        val fail = function.evaluate(TumorTestFactory.withBoneLesions(false))
        assertEvaluation(EvaluationResult.FAIL, fail)
    }
}