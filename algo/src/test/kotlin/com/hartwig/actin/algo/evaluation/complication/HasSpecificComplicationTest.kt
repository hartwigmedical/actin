package com.hartwig.actin.algo.evaluation.complication

import com.hartwig.actin.algo.evaluation.EvaluationAssert.assertEvaluation
import com.hartwig.actin.datamodel.algo.EvaluationResult
import com.hartwig.actin.datamodel.clinical.Complication
import org.junit.Test

class HasSpecificComplicationTest {

    @Test
    fun canEvaluate() {
        val function = HasSpecificComplication("name to find")
        assertEvaluation(EvaluationResult.UNDETERMINED, function.evaluate(ComplicationTestFactory.withComplications(null)))
        assertEvaluation(
            EvaluationResult.UNDETERMINED,
            function.evaluate(ComplicationTestFactory.withComplications(listOf(ComplicationTestFactory.yesInputComplication())))
        )
        assertEvaluation(EvaluationResult.FAIL, function.evaluate(ComplicationTestFactory.withComplications(emptyList())))
        val wrong: Complication = ComplicationTestFactory.complication(name = "just a name")
        assertEvaluation(EvaluationResult.FAIL, function.evaluate(ComplicationTestFactory.withComplication(wrong)))
        val match: Complication = ComplicationTestFactory.complication(name = "this includes name to find")
        assertEvaluation(EvaluationResult.PASS, function.evaluate(ComplicationTestFactory.withComplication(match)))
    }
}