package com.hartwig.actin.algo.evaluation.othercondition

import com.hartwig.actin.algo.evaluation.EvaluationAssert
import com.hartwig.actin.algo.icd.IcdConstants
import com.hartwig.actin.datamodel.algo.EvaluationResult
import com.hartwig.actin.icd.TestIcdFactory
import org.junit.Test

class HasInheritedPredispositionToBleedingOrThrombosisTest {
    private val function = HasInheritedPredispositionToBleedingOrThrombosis(TestIcdFactory.createTestModel())

    @Test
    fun `Should fail with no conditions`() {
        EvaluationAssert.assertEvaluation(
            EvaluationResult.FAIL, function.evaluate(OtherConditionTestFactory.withOtherConditions(emptyList()))
        )
    }

    @Test
    fun `Should fail with no relevant other condition`() {
        EvaluationAssert.assertEvaluation(
            EvaluationResult.FAIL, function.evaluate(
                OtherConditionTestFactory.withOtherConditions(
                    listOf(OtherConditionTestFactory.otherCondition(icdMainCode = "wrong"))
                )
            )
        )
    }

    @Test
    fun `Should pass with a condition with at least one correct DOID`() {
        EvaluationAssert.assertEvaluation(
            EvaluationResult.PASS,
            function.evaluate(
                OtherConditionTestFactory.withOtherCondition(
                    OtherConditionTestFactory.otherCondition(icdMainCode = IcdConstants.HEREDITARY_THROMBOPHILIA_CODE)
                )
            )
        )
    }

    @Test
    fun `Should pass with at least one condition with certain name`() {
        val conditions = listOf(
            OtherConditionTestFactory.otherCondition(name = "other name"),
            OtherConditionTestFactory.otherCondition(name = "disease FACTOR V LEIDEN")
        )

        EvaluationAssert.assertEvaluation(
            EvaluationResult.PASS, function.evaluate(OtherConditionTestFactory.withOtherConditions(conditions))
        )
    }
}