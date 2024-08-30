package com.hartwig.actin.algo.evaluation.infection

import com.hartwig.actin.algo.evaluation.EvaluationAssert.assertEvaluation
import com.hartwig.actin.datamodel.algo.EvaluationResult
import com.hartwig.actin.doid.TestDoidModelFactory
import org.junit.Test

private const val DOID_TO_FIND = "parent"
private const val CHILD_DOID = "child"

class HasSpecificInfectionTest {
    private val function = HasSpecificInfection(TestDoidModelFactory.createWithOneParentChild(DOID_TO_FIND, CHILD_DOID), DOID_TO_FIND)
    
    @Test
    fun `Should fail with no prior conditions`() {
        assertEvaluation(EvaluationResult.FAIL, function.evaluate(InfectionTestFactory.withPriorOtherConditions(emptyList())))
    }

    @Test
    fun `Should fail with prior conditions but no DOID`() {
        assertEvaluation(
            EvaluationResult.FAIL,
            function.evaluate(InfectionTestFactory.withPriorOtherCondition(InfectionTestFactory.priorOtherCondition()))
        )
    }

    @Test
    fun `Should fail with prior conditions and incorrect DOID`() {
        val conditions = listOf(InfectionTestFactory.priorOtherCondition(doids = setOf("not the correct doid")))
        assertEvaluation(EvaluationResult.FAIL, function.evaluate(InfectionTestFactory.withPriorOtherConditions(conditions)))
    }

    @Test
    fun `Should pass with prior conditions and child DOID`() {
        val condition = InfectionTestFactory.priorOtherCondition(doids = setOf(CHILD_DOID, "some other doid"))
        assertEvaluation(EvaluationResult.PASS, function.evaluate(InfectionTestFactory.withPriorOtherCondition(condition)))
    }

    @Test
    fun `Should pass with prior conditions and exact DOID`() {
        val exact = InfectionTestFactory.priorOtherCondition(doids = setOf(DOID_TO_FIND))
        assertEvaluation(EvaluationResult.PASS, function.evaluate(InfectionTestFactory.withPriorOtherCondition(exact)))
    }
}