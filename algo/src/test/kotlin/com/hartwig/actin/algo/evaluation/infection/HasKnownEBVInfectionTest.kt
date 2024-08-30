package com.hartwig.actin.algo.evaluation.infection

import com.hartwig.actin.algo.evaluation.EvaluationAssert.assertEvaluation
import com.hartwig.actin.datamodel.algo.EvaluationResult
import org.junit.Test

class HasKnownEBVInfectionTest {

    private val function = HasKnownEBVInfection()
    
    @Test
    fun `Should fail with no prior conditions`() {
        assertEvaluation(EvaluationResult.FAIL, function.evaluate(InfectionTestFactory.withPriorOtherConditions(emptyList())))
    }

    @Test
    fun `Should fail with prior conditions but no EBV`() {
        val conditions = listOf(InfectionTestFactory.priorOtherCondition(name = "this is nothing serious"))
        assertEvaluation(EvaluationResult.FAIL, function.evaluate(InfectionTestFactory.withPriorOtherConditions(conditions)))
    }

    @Test
    fun `Should pass with prior conditions and EBV`() {
        val conditions = listOf(InfectionTestFactory.priorOtherCondition(name = "This is real ${HasKnownEBVInfection.EBV_TERMS.first()}"))
        assertEvaluation(EvaluationResult.PASS, function.evaluate(InfectionTestFactory.withPriorOtherConditions(conditions)))
    }
}