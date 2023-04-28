package com.hartwig.actin.algo.evaluation.othercondition

import com.hartwig.actin.algo.datamodel.EvaluationResult
import com.hartwig.actin.algo.evaluation.EvaluationAssert.assertEvaluation
import com.hartwig.actin.clinical.datamodel.PriorOtherCondition
import com.hartwig.actin.doid.TestDoidModelFactory
import org.junit.Test

class HasHadPriorConditionWithDoidTest {
    @Test
    fun canEvaluate() {
        val doidToFind = "parent"
        val doidModel = TestDoidModelFactory.createWithOneParentChild(doidToFind, "child")
        val function = HasHadPriorConditionWithDoid(doidModel, doidToFind)

        // Test empty doid
        val conditions: MutableList<PriorOtherCondition> = mutableListOf()
        assertEvaluation(EvaluationResult.FAIL, function.evaluate(OtherConditionTestFactory.withPriorOtherConditions(conditions)))

        // Add a condition with no DOIDs
        conditions.add(OtherConditionTestFactory.builder().build())
        assertEvaluation(EvaluationResult.FAIL, function.evaluate(OtherConditionTestFactory.withPriorOtherConditions(conditions)))

        // Add a condition with not the correct DOID
        conditions.add(OtherConditionTestFactory.builder().addDoids("not the correct doid").build())
        assertEvaluation(EvaluationResult.FAIL, function.evaluate(OtherConditionTestFactory.withPriorOtherConditions(conditions)))

        // Add a condition with child DOID
        conditions.add(OtherConditionTestFactory.builder().addDoids("child", "some other doid").build())
        assertEvaluation(EvaluationResult.PASS, function.evaluate(OtherConditionTestFactory.withPriorOtherConditions(conditions)))

        // Also pass on the exact DOID
        val exact: PriorOtherCondition = OtherConditionTestFactory.builder().addDoids(doidToFind).build()
        assertEvaluation(EvaluationResult.PASS, function.evaluate(OtherConditionTestFactory.withPriorOtherCondition(exact)))
    }
}