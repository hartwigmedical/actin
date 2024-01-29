package com.hartwig.actin.algo.evaluation.complication

import com.hartwig.actin.algo.datamodel.EvaluationResult
import com.hartwig.actin.algo.evaluation.EvaluationAssert.assertEvaluation
import org.junit.Test

class HasAnyComplicationTest {
    @Test
    fun canEvaluate() {
        val function = HasAnyComplication()
        assertEvaluation(EvaluationResult.UNDETERMINED, function.evaluate(ComplicationTestFactory.withComplications(null)))
        assertEvaluation(EvaluationResult.FAIL, function.evaluate(ComplicationTestFactory.withComplications(emptyList())))
        assertEvaluation(
            EvaluationResult.PASS, function.evaluate(ComplicationTestFactory.withComplication(ComplicationTestFactory.complication()))
        )
    }
}