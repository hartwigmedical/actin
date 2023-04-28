package com.hartwig.actin.algo.evaluation.infection

import com.hartwig.actin.algo.datamodel.EvaluationResult
import com.hartwig.actin.algo.evaluation.EvaluationAssert.assertEvaluation
import com.hartwig.actin.clinical.datamodel.PriorOtherCondition
import org.junit.Test

class HasKnownEBVInfectionTest {
    @Test
    fun canEvaluate() {
        val function = HasKnownEBVInfection()

        // Test without conditions
        val conditions: MutableList<PriorOtherCondition> = mutableListOf()
        assertEvaluation(EvaluationResult.FAIL, function.evaluate(InfectionTestFactory.withPriorOtherConditions(conditions)))

        // Add with a wrong condition
        conditions.add(InfectionTestFactory.builder().name("this is nothing serious").build())
        assertEvaluation(EvaluationResult.FAIL, function.evaluate(InfectionTestFactory.withPriorOtherConditions(conditions)))

        // Test with ebv
        val realEBV = HasKnownEBVInfection.EBV_TERMS.iterator().next()
        conditions.add(InfectionTestFactory.builder().name("This is real $realEBV").build())
        assertEvaluation(EvaluationResult.PASS, function.evaluate(InfectionTestFactory.withPriorOtherConditions(conditions)))
    }
}