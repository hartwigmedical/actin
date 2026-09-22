package com.hartwig.actin.algo.evaluation.tumor

import com.hartwig.actin.algo.evaluation.EvaluationAssert.assertEvaluation
import com.hartwig.actin.datamodel.algo.EvaluationResult
import org.junit.jupiter.api.Test

class HasBoneMetastasesTest {
    
    private val function: HasBoneMetastases = HasBoneMetastases()

    @Test
    fun shouldBeUndeterminedWhenHasBoneLesionsIsNull() {
        val undetermined = function.evaluate(TumorTestFactory.withBoneLesions(null))
        assertEvaluation(EvaluationResult.UNDETERMINED, undetermined, "Undetermined if patient has bone metastases (missing lesion data)")
    }

    @Test
    fun shouldPassWhenHasBoneLesionsIsTrue() {
        val pass = function.evaluate(TumorTestFactory.withBoneLesions(true))
        assertEvaluation(EvaluationResult.PASS, pass, "Has bone metastases")
    }

    @Test
    fun shouldFailWhenHasBoneLesionsIsFalse() {
        val fail = function.evaluate(TumorTestFactory.withBoneLesions(false))
        assertEvaluation(EvaluationResult.FAIL, fail, "No bone metastases")
    }
}