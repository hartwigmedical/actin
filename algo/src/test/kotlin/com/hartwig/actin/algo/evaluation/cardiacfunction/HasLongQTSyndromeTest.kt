package com.hartwig.actin.algo.evaluation.cardiacfunction

import com.hartwig.actin.algo.evaluation.EvaluationAssert.assertEvaluation
import com.hartwig.actin.algo.evaluation.othercondition.OtherConditionTestFactory
import com.hartwig.actin.algo.icd.IcdConstants
import com.hartwig.actin.datamodel.algo.EvaluationResult
import com.hartwig.actin.icd.TestIcdFactory
import org.junit.Test

class HasLongQTSyndromeTest {
    private val function = HasLongQTSyndrome(TestIcdFactory.createTestModel())

    @Test
    fun `Should fail with no conditions`() {
        assertEvaluation(EvaluationResult.FAIL, function.evaluate(OtherConditionTestFactory.withPriorOtherConditions((emptyList()))))
    }

    @Test
    fun `Should fail with other condition`() {
        val condition = OtherConditionTestFactory.priorOtherCondition(icdMainCode = IcdConstants.PNEUMOTHORAX_CODE)
        assertEvaluation(
            EvaluationResult.FAIL,
            function.evaluate(OtherConditionTestFactory.withPriorOtherCondition(condition))
        )
    }

    @Test
    fun `Should pass with matching condition`() {
        val condition = OtherConditionTestFactory.priorOtherCondition(icdMainCode = IcdConstants.LONG_QT_SYNDROME_CODE)
        assertEvaluation(
            EvaluationResult.PASS,
            function.evaluate(OtherConditionTestFactory.withPriorOtherCondition(condition))
        )
    }
}