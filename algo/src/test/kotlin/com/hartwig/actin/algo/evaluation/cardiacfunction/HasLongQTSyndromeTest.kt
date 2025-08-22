package com.hartwig.actin.algo.evaluation.cardiacfunction

import com.hartwig.actin.algo.evaluation.EvaluationAssert.assertEvaluation
import com.hartwig.actin.algo.evaluation.comorbidity.ComorbidityTestFactory
import com.hartwig.actin.algo.icd.IcdConstants
import com.hartwig.actin.datamodel.algo.EvaluationResult
import com.hartwig.actin.icd.TestIcdFactory
import org.junit.Test

class HasLongQTSyndromeTest {
    private val function = HasLongQTSyndrome(TestIcdFactory.createTestModel())

    @Test
    fun `Should fail with no conditions`() {
        assertEvaluation(EvaluationResult.FAIL, function.evaluate(ComorbidityTestFactory.withOtherConditions((emptyList()))))
    }

    @Test
    fun `Should fail with other condition`() {
        val condition = ComorbidityTestFactory.otherCondition(icdMainCode = IcdConstants.PNEUMOTHORAX_CODE)
        assertEvaluation(
            EvaluationResult.FAIL,
            function.evaluate(ComorbidityTestFactory.withOtherCondition(condition))
        )
    }

    @Test
    fun `Should pass with matching condition`() {
        val condition = ComorbidityTestFactory.otherCondition(icdMainCode = IcdConstants.LONG_QT_SYNDROME_CODE)
        assertEvaluation(
            EvaluationResult.PASS,
            function.evaluate(ComorbidityTestFactory.withOtherCondition(condition))
        )
    }
}