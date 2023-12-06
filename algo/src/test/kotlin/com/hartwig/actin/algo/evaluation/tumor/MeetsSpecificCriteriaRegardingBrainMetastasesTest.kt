package com.hartwig.actin.algo.evaluation.tumor

import com.hartwig.actin.algo.datamodel.EvaluationResult
import com.hartwig.actin.algo.evaluation.EvaluationAssert
import org.junit.Test

class MeetsSpecificCriteriaRegardingBrainMetastasesTest {

    @Test
    fun `Should return undetermined in case of having brain metastases`() {
        val evaluation = FUNCTION.evaluate(TumorTestFactory.withBrainLesions(true))
        EvaluationAssert.assertEvaluation(EvaluationResult.UNDETERMINED, evaluation)
    }

    @Test
    fun `Should return undetermined in case of missing brain metastases data and having CNS lesions`() {
        val evaluation = FUNCTION.evaluate(TumorTestFactory.withBrainAndCnsLesions(null, true))
        EvaluationAssert.assertEvaluation(EvaluationResult.UNDETERMINED, evaluation)
    }

    @Test
    fun `Should return fail in case of missing brain metastases data and no CNS lesions`() {
        val evaluation = FUNCTION.evaluate(TumorTestFactory.withBrainAndCnsLesions(null, false))
        EvaluationAssert.assertEvaluation(EvaluationResult.FAIL, evaluation)
    }

    @Test
    fun `Should return fail in case of missing brain metastases data and missing CNS lesions data`() {
        val evaluation = FUNCTION.evaluate(TumorTestFactory.withBrainAndCnsLesions(null, null))
        EvaluationAssert.assertEvaluation(EvaluationResult.FAIL, evaluation)
    }

    @Test
    fun `Should return fail in case of no brain metastases`() {
        val evaluation = FUNCTION.evaluate(TumorTestFactory.withBrainLesions(false))
        EvaluationAssert.assertEvaluation(EvaluationResult.FAIL, evaluation)
    }

    companion object {
        private val FUNCTION = MeetsSpecificCriteriaRegardingBrainMetastases()
    }
}