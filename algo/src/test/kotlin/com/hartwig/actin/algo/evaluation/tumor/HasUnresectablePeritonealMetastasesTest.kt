package com.hartwig.actin.algo.evaluation.tumor

import com.hartwig.actin.algo.datamodel.EvaluationResult
import com.hartwig.actin.algo.evaluation.EvaluationAssert
import org.junit.Test

class HasUnresectablePeritonealMetastasesTest {

    @Test
    fun canEvaluate() {
        val function = HasUnresectablePeritonealMetastases()
        EvaluationAssert.assertEvaluation(
            EvaluationResult.WARN,
            function.evaluate(TumorTestFactory.withOtherLesions(listOf("Abdominal lesion located in peritoneum")))
        )
        EvaluationAssert.assertEvaluation(
            EvaluationResult.WARN,
            function.evaluate(TumorTestFactory.withOtherLesions(listOf("Multiple depositions abdominal and peritoneal")))
        )
        EvaluationAssert.assertEvaluation(
            EvaluationResult.FAIL,
            function.evaluate(TumorTestFactory.withOtherLesions(listOf("Lymph node")))
        )
    }
}