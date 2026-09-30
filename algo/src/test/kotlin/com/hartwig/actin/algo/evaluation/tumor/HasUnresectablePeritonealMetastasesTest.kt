package com.hartwig.actin.algo.evaluation.tumor

import com.hartwig.actin.algo.evaluation.EvaluationAssert
import com.hartwig.actin.datamodel.algo.EvaluationResult
import org.junit.jupiter.api.Test

class HasUnresectablePeritonealMetastasesTest {

    private val function = HasUnresectablePeritonealMetastases()

    @Test
    fun `Should be undetermined if other lesions are unknown`() {
        EvaluationAssert.assertEvaluation(
            EvaluationResult.UNDETERMINED,
            function.evaluate(TumorTestFactory.withOtherLesions(null)),
            "Unresectable peritoneal metastases undetermined (metastases data missing)"
        )
    }

    @Test
    fun `Should fail if patient has no peritoneal metastases`() {
        EvaluationAssert.assertEvaluation(
            EvaluationResult.FAIL,
            function.evaluate(TumorTestFactory.withOtherLesions(listOf("retroperitoneal lesions"))),
            "No unresectable peritoneal metastases"
        )
        EvaluationAssert.assertEvaluation(
            EvaluationResult.FAIL,
            function.evaluate(TumorTestFactory.withOtherLesions(listOf("metastases in subperitoneal region"))),
            "No unresectable peritoneal metastases"
        )
        EvaluationAssert.assertEvaluation(
            EvaluationResult.FAIL,
            function.evaluate(TumorTestFactory.withOtherLesions(listOf("Lymph node"))),
            "No unresectable peritoneal metastases"
        )
    }

    @Test
    fun `Should fail if patient has no other lesions`() {
        EvaluationAssert.assertEvaluation(
            EvaluationResult.FAIL,
            function.evaluate(TumorTestFactory.withOtherLesions(emptyList())),
            "No unresectable peritoneal metastases"
        )
    }

    @Test
    fun `Should warn if patient has peritoneal metastases`() {
        EvaluationAssert.assertEvaluation(
            EvaluationResult.WARN,
            function.evaluate(TumorTestFactory.withOtherLesions(listOf("Abdominal lesion located in Peritoneum"))),
            "Undetermined if peritoneal metastases are unresectable"
        )
        EvaluationAssert.assertEvaluation(
            EvaluationResult.WARN,
            function.evaluate(TumorTestFactory.withOtherLesions(listOf("Multiple depositions abdominal and peritoneal"))),
            "Undetermined if peritoneal metastases are unresectable"
        )
        EvaluationAssert.assertEvaluation(
            EvaluationResult.WARN,
            function.evaluate(TumorTestFactory.withOtherLesions(listOf("intraperitoneal"))),
            "Undetermined if peritoneal metastases are unresectable"
        )
    }

    @Test
    fun `Should warn if patient has suspected peritoneal metastases`() {
        EvaluationAssert.assertEvaluation(
            EvaluationResult.WARN,
            function.evaluate(TumorTestFactory.withOtherSuspectedLesions(listOf("peritoneal"))),
            "Undetermined if (suspected) peritoneal metastases are unresectable"
        )
    }
}