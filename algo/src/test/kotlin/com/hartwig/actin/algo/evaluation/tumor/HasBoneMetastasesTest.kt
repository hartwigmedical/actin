package com.hartwig.actin.algo.evaluation.tumor

import com.hartwig.actin.algo.evaluation.EvaluationAssert.assertEvaluation
import com.hartwig.actin.datamodel.algo.EvaluationResult
import org.junit.jupiter.api.Test

class HasBoneMetastasesTest {
    
    private val function: HasBoneMetastases = HasBoneMetastases()

    @Test
    fun shouldBeUndeterminedWhenHasBoneLesionsIsNull() {
        val undetermined = function.evaluate(TumorTestFactory.withBoneLesions(null))
        assertEvaluation(
            EvaluationResult.UNDETERMINED,
            undetermined,
            "Undetermined if bone metastases based on provided lesions"
        )
    }

    @Test
    fun shouldPassWhenHasBoneLesionsIsTrue() {
        val pass = function.evaluate(TumorTestFactory.withBoneLesions(true))
        assertEvaluation(EvaluationResult.PASS, pass, "Bone metastases in provided lesions")
    }

    @Test
    fun shouldFailWhenHasBoneLesionsIsFalse() {
        val fail = function.evaluate(TumorTestFactory.withBoneLesions(false))
        assertEvaluation(EvaluationResult.FAIL, fail, "No bone metastases in provided lesions")
    }
}