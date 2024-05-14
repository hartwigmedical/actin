package com.hartwig.actin.algo.evaluation.tumor

import com.hartwig.actin.algo.datamodel.EvaluationResult
import com.hartwig.actin.algo.evaluation.EvaluationAssert.assertEvaluation
import org.junit.Test

class MeetsSpecificCriteriaRegardingLiverMetastasesTest {

    @Test
    fun `Should return undetermined in case of missing liver metastases data`() {
        val evaluation = FUNCTION.evaluate(TestTumorFactory.withLiverLesions(null))
        assertEvaluation(EvaluationResult.UNDETERMINED, evaluation)
    }

    @Test
    fun `Should return undetermined in case of having liver metastases`() {
        val evaluation = FUNCTION.evaluate(TestTumorFactory.withLiverLesions(true))
        assertEvaluation(EvaluationResult.UNDETERMINED, evaluation)
    }

    @Test
    fun `Should return fail in case of no liver metastases`() {
        val evaluation = FUNCTION.evaluate(TestTumorFactory.withLiverLesions(false))
        assertEvaluation(EvaluationResult.FAIL, evaluation)
    }

    companion object {
        private val FUNCTION = MeetsSpecificCriteriaRegardingLiverMetastases()
    }
}