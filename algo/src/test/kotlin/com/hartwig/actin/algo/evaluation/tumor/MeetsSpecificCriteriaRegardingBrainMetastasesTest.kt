package com.hartwig.actin.algo.evaluation.tumor

import com.hartwig.actin.algo.evaluation.EvaluationAssert
import com.hartwig.actin.datamodel.algo.EvaluationResult
import org.junit.jupiter.api.Test

class MeetsSpecificCriteriaRegardingBrainMetastasesTest {

    @Test
    fun `Should return undetermined in case of having brain metastases`() {
        val evaluation = FUNCTION.evaluate(TumorTestFactory.withBrainLesions(true))
        EvaluationAssert.assertEvaluation(
            EvaluationResult.UNDETERMINED,
            evaluation,
            "Undetermined if study specific criteria regarding brain metastases are met"
        )
    }

    @Test
    fun `Should return undetermined in case of having active brain metastases`() {
        val evaluation = FUNCTION.evaluate(TumorTestFactory.withBrainLesionStatus(null, true))
        EvaluationAssert.assertEvaluation(
            EvaluationResult.UNDETERMINED,
            evaluation,
            "Undetermined if study specific criteria regarding brain metastases are met"
        )
    }

    @Test
    fun `Should return undetermined in case of missing brain metastases data and having CNS lesions`() {
        val evaluation = FUNCTION.evaluate(TumorTestFactory.withBrainAndCnsLesions(null, true))
        EvaluationAssert.assertEvaluation(
            EvaluationResult.UNDETERMINED,
            evaluation,
            "Undetermined if study specific criteria regarding brain metastases are met"
        )
    }

    @Test
    fun `Should return undetermined in case of missing brain metastases data and having suspected CNS lesions`() {
        val evaluation = FUNCTION.evaluate(TumorTestFactory.withBrainAndCnsLesions(null, false, false, true))
        EvaluationAssert.assertEvaluation(
            EvaluationResult.UNDETERMINED,
            evaluation,
            "Undetermined if study specific criteria regarding suspected brain metastases are met"
        )
    }

    @Test
    fun `Should return undetermined in case of no brain metastases and having suspected brain lesions`() {
        val evaluation = FUNCTION.evaluate(TumorTestFactory.withBrainAndCnsLesions(false, false, true, false))
        EvaluationAssert.assertEvaluation(
            EvaluationResult.UNDETERMINED,
            evaluation,
            "Undetermined if study specific criteria regarding suspected brain metastases are met"
        )
    }

    @Test
    fun `Should return undetermined in case of missing brain metastases data and no CNS lesions`() {
        val evaluation = FUNCTION.evaluate(TumorTestFactory.withBrainAndCnsLesions(null, false))
        EvaluationAssert.assertEvaluation(
            EvaluationResult.UNDETERMINED,
            evaluation,
            "Undetermined if specific criteria regarding brain metastases are met (brain lesions data missing)"
        )
    }

    @Test
    fun `Should return undetermined in case of missing brain metastases data and missing CNS lesions data`() {
        val evaluation = FUNCTION.evaluate(TumorTestFactory.withBrainAndCnsLesions(null, null))
        EvaluationAssert.assertEvaluation(
            EvaluationResult.UNDETERMINED,
            evaluation,
            "Undetermined if specific criteria regarding brain metastases are met (brain lesions data missing)"
        )
    }

    @Test
    fun `Should return fail in case of no brain metastases`() {
        val evaluation = FUNCTION.evaluate(TumorTestFactory.withBrainLesions(false))
        EvaluationAssert.assertEvaluation(
            EvaluationResult.FAIL,
            evaluation,
            "No brain metastases present hence won't meet study specific criteria regarding brain metastases"
        )
    }

    companion object {
        private val FUNCTION = MeetsSpecificCriteriaRegardingBrainMetastases()
    }
}