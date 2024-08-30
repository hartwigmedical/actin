package com.hartwig.actin.algo.evaluation.tumor

import com.hartwig.actin.algo.evaluation.EvaluationAssert.assertEvaluation
import com.hartwig.actin.datamodel.algo.EvaluationResult
import org.junit.Test

class MeetsSpecificCriteriaRegardingLiverMetastasesTest {

    @Test
    fun `Should return undetermined in case of missing liver metastases data`() {
        val evaluation = FUNCTION.evaluate(TumorTestFactory.withLiverLesions(null))
        assertEvaluation(EvaluationResult.UNDETERMINED, evaluation)
    }

    @Test
    fun `Should return undetermined in case of having liver metastases`() {
        val evaluation = FUNCTION.evaluate(TumorTestFactory.withLiverLesions(true))
        assertEvaluation(EvaluationResult.UNDETERMINED, evaluation)
    }

    @Test
    fun `Should return fail in case of no liver metastases`() {
        val evaluation = FUNCTION.evaluate(TumorTestFactory.withLiverLesions(false))
        assertEvaluation(EvaluationResult.FAIL, evaluation)
    }

    companion object {
        private val FUNCTION = MeetsSpecificCriteriaRegardingLiverMetastases()
    }
}