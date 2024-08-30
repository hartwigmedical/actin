package com.hartwig.actin.algo.evaluation.complication

import com.hartwig.actin.algo.evaluation.EvaluationAssert.assertEvaluation
import com.hartwig.actin.datamodel.algo.EvaluationResult
import com.hartwig.actin.datamodel.clinical.Complication
import org.junit.Test

class HasComplicationOfCategoryTest {

    @Test
    fun canEvaluate() {
        val function = HasComplicationOfCategory("category X")
        assertEvaluation(EvaluationResult.UNDETERMINED, function.evaluate(ComplicationTestFactory.withComplications(null)))
        assertEvaluation(
            EvaluationResult.UNDETERMINED,
            function.evaluate(ComplicationTestFactory.withComplications(listOf(ComplicationTestFactory.yesInputComplication())))
        )
        assertEvaluation(EvaluationResult.FAIL, function.evaluate(ComplicationTestFactory.withComplications(emptyList())))

        val different: Complication = ComplicationTestFactory.complication(categories = setOf("this is category Y"))
        assertEvaluation(EvaluationResult.FAIL, function.evaluate(ComplicationTestFactory.withComplication(different)))

        val match: Complication = ComplicationTestFactory.complication(categories = setOf("this is category X"))
        assertEvaluation(EvaluationResult.PASS, function.evaluate(ComplicationTestFactory.withComplication(match)))
    }
}