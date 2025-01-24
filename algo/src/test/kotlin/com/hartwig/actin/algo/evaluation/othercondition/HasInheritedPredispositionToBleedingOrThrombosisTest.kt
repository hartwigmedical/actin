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
            EvaluationResult.FAIL, function.evaluate(ComorbidityTestFactory.withOtherConditions(emptyList()))
        )
    }

    @Test
    fun `Should fail with no relevant other condition`() {
        EvaluationAssert.assertEvaluation(
            EvaluationResult.FAIL, function.evaluate(
                ComorbidityTestFactory.withOtherConditions(
                    listOf(ComorbidityTestFactory.otherCondition(icdMainCode = "wrong"))
                )
            )
        )
    }

    @Test
    fun `Should pass with a condition with at least one correct DOID`() {
        EvaluationAssert.assertEvaluation(
            EvaluationResult.PASS,
            function.evaluate(
                ComorbidityTestFactory.withOtherCondition(
                    ComorbidityTestFactory.otherCondition(icdMainCode = IcdConstants.HEREDITARY_THROMBOPHILIA_CODE)
                )
            )
        )
    }

    @Test
    fun `Should pass with at least one condition with certain name`() {
        val conditions = listOf(
            ComorbidityTestFactory.otherCondition(name = "other name"),
            ComorbidityTestFactory.otherCondition(name = "disease FACTOR V LEIDEN")
        )

        EvaluationAssert.assertEvaluation(
            EvaluationResult.PASS, function.evaluate(ComorbidityTestFactory.withOtherConditions(conditions))
        )
    }
}