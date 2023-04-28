package com.hartwig.actin.algo.evaluation.infection

import com.hartwig.actin.algo.datamodel.EvaluationResult
import com.hartwig.actin.algo.evaluation.EvaluationAssert.assertEvaluation
import com.hartwig.actin.clinical.datamodel.PriorOtherCondition
import com.hartwig.actin.doid.TestDoidModelFactory
import org.junit.Test

class HasSpecificInfectionTest {
    @Test
    fun canEvaluate() {
        val doidToFind = "parent"
        val doidModel = TestDoidModelFactory.createWithOneParentChild(doidToFind, "child")
        val function = HasSpecificInfection(doidModel, doidToFind)

        // Test empty doid
        val conditions: MutableList<PriorOtherCondition> = mutableListOf()
        assertEvaluation(EvaluationResult.FAIL, function.evaluate(InfectionTestFactory.withPriorOtherConditions(conditions)))

        // Add a condition with no DOIDs
        conditions.add(InfectionTestFactory.builder().build())
        assertEvaluation(EvaluationResult.FAIL, function.evaluate(InfectionTestFactory.withPriorOtherConditions(conditions)))

        // Add a condition with not the correct DOID
        conditions.add(InfectionTestFactory.builder().addDoids("not the correct doid").build())
        assertEvaluation(EvaluationResult.FAIL, function.evaluate(InfectionTestFactory.withPriorOtherConditions(conditions)))

        // Add a condition with child DOID
        conditions.add(InfectionTestFactory.builder().addDoids("child", "some other doid").build())
        assertEvaluation(EvaluationResult.PASS, function.evaluate(InfectionTestFactory.withPriorOtherConditions(conditions)))

        // Also pass on the exact DOID
        val exact: PriorOtherCondition = InfectionTestFactory.builder().addDoids(doidToFind).build()
        assertEvaluation(EvaluationResult.PASS, function.evaluate(InfectionTestFactory.withPriorOtherCondition(exact)))
    }
}