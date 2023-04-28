package com.hartwig.actin.algo.evaluation.othercondition

import com.hartwig.actin.algo.datamodel.EvaluationResult
import com.hartwig.actin.algo.evaluation.EvaluationAssert.assertEvaluation
import com.hartwig.actin.clinical.datamodel.PriorOtherCondition
import org.junit.Test

class HasHadPriorConditionWithNameTest {
    @Test
    fun canEvaluate() {
        val nameToFind = "severe condition"
        val function = HasHadPriorConditionWithName(nameToFind)

        // Test empty doid
        val conditions: MutableList<PriorOtherCondition> = mutableListOf()
        assertEvaluation(EvaluationResult.FAIL, function.evaluate(OtherConditionTestFactory.withPriorOtherConditions(conditions)))

        // Add a condition with wrong name
        conditions.add(OtherConditionTestFactory.builder().name("benign condition").build())
        assertEvaluation(EvaluationResult.FAIL, function.evaluate(OtherConditionTestFactory.withPriorOtherConditions(conditions)))

        // Add a condition with right name
        conditions.add(OtherConditionTestFactory.builder().name("very severe condition").build())
        assertEvaluation(EvaluationResult.PASS, function.evaluate(OtherConditionTestFactory.withPriorOtherConditions(conditions)))
    }
}