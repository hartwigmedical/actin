package com.hartwig.actin.algo.evaluation.othercondition

import com.hartwig.actin.algo.datamodel.EvaluationResult
import com.hartwig.actin.algo.doid.DoidConstants
import com.hartwig.actin.algo.evaluation.EvaluationAssert
import com.hartwig.actin.doid.TestDoidModelFactory
import org.junit.Test

class HasInheritedPredispositionToBleedingOrThrombosisTest {
    private val function = HasInheritedPredispositionToBleedingOrThrombosis(TestDoidModelFactory.createMinimalTestDoidModel())

    @Test
    fun `Should fail with no prior conditions`() {
        EvaluationAssert.assertEvaluation(
            EvaluationResult.FAIL,
            function.evaluate(OtherConditionTestFactory.withPriorOtherConditions(emptyList()))
        )
    }

    @Test
    fun `Should fail with no relevant prior other condition`() {
        EvaluationAssert.assertEvaluation(
            EvaluationResult.FAIL, function.evaluate(
                OtherConditionTestFactory.withPriorOtherConditions(
                    listOf(
                        OtherConditionTestFactory.priorOtherCondition(doids = setOf("wrong doid")),
                    )
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
                    OtherConditionTestFactory.priorOtherCondition(
                        doids = setOf(
                            DoidConstants.AUTOSOMAL_HEMOPHILIA_A, "wrong doid"
                        )
                    )
                )
            )
        )
    }
}