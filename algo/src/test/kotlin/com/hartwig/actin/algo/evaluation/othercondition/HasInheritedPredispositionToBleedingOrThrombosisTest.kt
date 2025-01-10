package com.hartwig.actin.algo.evaluation.othercondition

import com.hartwig.actin.algo.evaluation.EvaluationAssert
import com.hartwig.actin.algo.icd.IcdConstants
import com.hartwig.actin.datamodel.algo.EvaluationResult
import com.hartwig.actin.icd.TestIcdFactory
import org.junit.Test

class HasInheritedPredispositionToBleedingOrThrombosisTest {
    private val function = HasInheritedPredispositionToBleedingOrThrombosis(TestIcdFactory.createTestModel())

    @Test
    fun `Should fail with no prior conditions`() {
        EvaluationAssert.assertEvaluation(
            EvaluationResult.FAIL, function.evaluate(OtherConditionTestFactory.withPriorOtherConditions(emptyList()))
        )
    }

    @Test
    fun `Should fail with no relevant prior other condition`() {
        EvaluationAssert.assertEvaluation(
            EvaluationResult.FAIL, function.evaluate(
                OtherConditionTestFactory.withPriorOtherConditions(
                    listOf(OtherConditionTestFactory.priorOtherCondition(icdMainCode = "wrong"))
                )
            )
        )
    }

    @Test
    fun `Should pass with a condition with at least one correct DOID`() {
        EvaluationAssert.assertEvaluation(
            EvaluationResult.PASS,
            function.evaluate(
                OtherConditionTestFactory.withPriorOtherCondition(
                    OtherConditionTestFactory.priorOtherCondition(icdMainCode = IcdConstants.HEREDITARY_THROMBOPHILIA_CODE)
                )
            )
        )
    }

    @Test
    fun `Should pass with at least one condition with certain name`() {
        val conditions = listOf(
            OtherConditionTestFactory.priorOtherCondition(name = "other name"),
            OtherConditionTestFactory.priorOtherCondition(name = "disease FACTOR V LEIDEN")
        )

        EvaluationAssert.assertEvaluation(
            EvaluationResult.PASS, function.evaluate(OtherConditionTestFactory.withPriorOtherConditions(conditions))
        )
    }
}