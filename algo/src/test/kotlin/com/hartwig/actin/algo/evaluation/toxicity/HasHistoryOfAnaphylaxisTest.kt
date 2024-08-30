package com.hartwig.actin.algo.evaluation.toxicity

import com.hartwig.actin.algo.evaluation.EvaluationAssert.assertEvaluation
import com.hartwig.actin.datamodel.algo.EvaluationResult
import org.junit.Test

class HasHistoryOfAnaphylaxisTest {
    @Test
    fun canEvaluate() {
        val function = HasHistoryOfAnaphylaxis()
        assertEvaluation(EvaluationResult.FAIL, function.evaluate(ToxicityTestFactory.withIntolerances(emptyList())))
        assertEvaluation(
            EvaluationResult.UNDETERMINED,
            function.evaluate(ToxicityTestFactory.withIntolerance(ToxicityTestFactory.intolerance()))
        )
    }
}