package com.hartwig.actin.algo.evaluation.complication

import com.hartwig.actin.algo.datamodel.EvaluationResult
import com.hartwig.actin.algo.evaluation.EvaluationAssert.assertEvaluation
import com.hartwig.actin.clinical.datamodel.Complication
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
        val wrong: Complication = ComplicationTestFactory.builder().name("just a name").build()
        assertEvaluation(EvaluationResult.FAIL, function.evaluate(ComplicationTestFactory.withComplication(wrong)))
        val match: Complication = ComplicationTestFactory.builder().name("this includes name to find").build()
        assertEvaluation(EvaluationResult.PASS, function.evaluate(ComplicationTestFactory.withComplication(match)))
    }
}