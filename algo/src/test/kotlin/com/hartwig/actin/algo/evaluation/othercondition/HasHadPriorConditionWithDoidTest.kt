package com.hartwig.actin.algo.evaluation.othercondition

import com.hartwig.actin.algo.evaluation.EvaluationAssert.assertEvaluation
import com.hartwig.actin.datamodel.algo.EvaluationResult
import com.hartwig.actin.datamodel.clinical.PriorOtherCondition
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
        conditions.add(OtherConditionTestFactory.priorOtherCondition())
        assertEvaluation(EvaluationResult.FAIL, function.evaluate(OtherConditionTestFactory.withPriorOtherConditions(conditions)))

        // Add a condition with not the correct DOID
        conditions.add(OtherConditionTestFactory.priorOtherCondition(doids = setOf("not the correct doid")))
        assertEvaluation(EvaluationResult.FAIL, function.evaluate(OtherConditionTestFactory.withPriorOtherConditions(conditions)))

        // Add a condition with child DOID
        conditions.add(OtherConditionTestFactory.priorOtherCondition(doids = setOf("child", "some other doid")))
        assertEvaluation(EvaluationResult.PASS, function.evaluate(OtherConditionTestFactory.withPriorOtherConditions(conditions)))

        // Also pass on the exact DOID
        val exact: PriorOtherCondition = OtherConditionTestFactory.priorOtherCondition(doids = setOf(doidToFind))
        assertEvaluation(EvaluationResult.PASS, function.evaluate(OtherConditionTestFactory.withPriorOtherCondition(exact)))
    }
}