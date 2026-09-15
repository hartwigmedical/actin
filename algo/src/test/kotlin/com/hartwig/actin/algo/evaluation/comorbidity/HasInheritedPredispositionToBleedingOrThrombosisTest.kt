package com.hartwig.actin.algo.evaluation.comorbidity

import com.hartwig.actin.algo.evaluation.EvaluationAssert
import com.hartwig.actin.algo.icd.IcdConstants
import com.hartwig.actin.datamodel.algo.EvaluationResult
import com.hartwig.actin.icd.TestIcdFactory
import org.junit.jupiter.api.Test

class HasInheritedPredispositionToBleedingOrThrombosisTest {
    private val function = HasInheritedPredispositionToBleedingOrThrombosis(TestIcdFactory.createTestModel())

    @Test
    fun `Should fail with no conditions`() {
        EvaluationAssert.assertEvaluation(
            EvaluationResult.FAIL, function.evaluate(ComorbidityTestFactory.withOtherConditions(emptyList())),
            "No history of (typically) inherited predisposition to bleeding or thrombosis"
        )
    }

    @Test
    fun `Should fail with no relevant other condition`() {
        EvaluationAssert.assertEvaluation(
            EvaluationResult.FAIL, function.evaluate(
                ComorbidityTestFactory.withOtherConditions(
                    listOf(ComorbidityTestFactory.otherCondition(icdMainCode = "wrong"))
                )
            ),
            "No history of (typically) inherited predisposition to bleeding or thrombosis"
        )
    }

    @Test
    fun `Should pass with a condition with at least one correct DOID`() {
        EvaluationAssert.assertEvaluation(
            EvaluationResult.PASS,
            function.evaluate(
                ComorbidityTestFactory.withOtherCondition(
                    ComorbidityTestFactory.otherCondition(name = "hereditary thrombophilia", icdMainCode = IcdConstants.HEREDITARY_THROMBOPHILIA_CODE)
                )
            ),
            "History of (typically) inherited predisposition to bleeding or thrombosis: hereditary thrombophilia"
        )
    }

    @Test
    fun `Should pass with at least one condition with certain name`() {
        val conditions = listOf(
            ComorbidityTestFactory.otherCondition(name = "other name"),
            ComorbidityTestFactory.otherCondition(name = "disease FACTOR V LEIDEN")
        )

        EvaluationAssert.assertEvaluation(
            EvaluationResult.PASS, function.evaluate(ComorbidityTestFactory.withOtherConditions(conditions)),
            "History of (typically) inherited predisposition to bleeding or thrombosis: Factor V Leiden"
        )
    }
}