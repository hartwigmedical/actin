package com.hartwig.actin.algo.evaluation.othercondition

import com.hartwig.actin.algo.evaluation.EvaluationAssert.assertEvaluation
import com.hartwig.actin.algo.icd.IcdConstants
import com.hartwig.actin.datamodel.algo.EvaluationResult
import com.hartwig.actin.icd.TestIcdFactory
import com.hartwig.actin.trial.input.datamodel.NyhaClass
import org.junit.Test

class HasHistoryOfCongestiveHeartFailureWithNYHATest {

    private val function = HasHistoryOfCongestiveHeartFailureWithNYHA(NyhaClass.III, TestIcdFactory.createTestModel())

    @Test
    fun `Should pass if congestive heart failure with at least requested NYHA class in history`() {
        listOf(IcdConstants.NYHA_CLASS_3_CODE, IcdConstants.NYHA_CLASS_4_CODE).forEach {
            assertEvaluation(
                EvaluationResult.PASS, function.evaluate(
                    OtherConditionTestFactory.withOtherCondition(
                        OtherConditionTestFactory.otherCondition(
                            icdMainCode = IcdConstants.CONGESTIVE_HEART_FAILURE_CODE,
                            icdExtensionCode = it
                        )
                    )
                )
            )
        }
    }

    @Test
    fun `Should evaluate to undetermined if congestive heart failure with unknown NYHA class in history`() {
        assertEvaluation(
            EvaluationResult.UNDETERMINED, function.evaluate(
                OtherConditionTestFactory.withOtherCondition(
                    OtherConditionTestFactory.otherCondition(
                        icdMainCode = IcdConstants.CONGESTIVE_HEART_FAILURE_CODE,
                        icdExtensionCode = null
                    )
                )
            )
        )
    }

    @Test
    fun `Should fail for congestive heart failure with NYHA class lower than requested`() {
        assertEvaluation(
            EvaluationResult.FAIL, function.evaluate(
                OtherConditionTestFactory.withOtherCondition(
                    OtherConditionTestFactory.otherCondition(
                        icdMainCode = IcdConstants.CONGESTIVE_HEART_FAILURE_CODE,
                        icdExtensionCode = IcdConstants.NYHA_CLASS_2_CODE
                    )
                )
            )
        )
    }

    @Test
    fun `Should fail for wrong condition`() {
        assertEvaluation(
            EvaluationResult.FAIL, function.evaluate(
                OtherConditionTestFactory.withOtherCondition(
                    OtherConditionTestFactory.otherCondition(
                        icdMainCode = IcdConstants.PNEUMOTHORAX_CODE,
                        icdExtensionCode = IcdConstants.NYHA_CLASS_4_CODE
                    )
                )
            )
        )
    }

    @Test
    fun `Should fail for empty history`() {
        assertEvaluation(
            EvaluationResult.FAIL, function.evaluate(OtherConditionTestFactory.withOtherConditions(emptyList()))
        )
    }
}
