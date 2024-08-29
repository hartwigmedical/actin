package com.hartwig.actin.algo.evaluation.complication

import com.hartwig.actin.algo.evaluation.EvaluationAssert.assertEvaluation
import com.hartwig.actin.datamodel.algo.EvaluationResult
import com.hartwig.actin.datamodel.clinical.Complication
import org.junit.Test

class HasLeptomeningealDiseaseTest {
    @Test
    fun canEvaluate() {
        val function = HasLeptomeningealDisease()
        assertEvaluation(EvaluationResult.FAIL, function.evaluate(ComplicationTestFactory.withComplications(emptyList())))

        val different: Complication = ComplicationTestFactory.complication(categories = setOf("other complication"))
        assertEvaluation(EvaluationResult.FAIL, function.evaluate(ComplicationTestFactory.withComplication(different)))

        val matching: Complication = ComplicationTestFactory.complication(categories = setOf("leptomeningeal disease type 1"))
        assertEvaluation(EvaluationResult.PASS, function.evaluate(ComplicationTestFactory.withComplication(matching)))
        assertEvaluation(EvaluationResult.FAIL, function.evaluate(ComplicationTestFactory.withCnsLesion("just a lesion")))
        assertEvaluation(
            EvaluationResult.WARN,
            function.evaluate(ComplicationTestFactory.withCnsLesion("carcinomatous furious meningitis"))
        )
    }
}