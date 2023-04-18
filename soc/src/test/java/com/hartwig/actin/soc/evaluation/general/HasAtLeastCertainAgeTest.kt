package com.hartwig.actin.soc.evaluation.general

import com.hartwig.actin.algo.datamodel.EvaluationResult
import com.hartwig.actin.soc.evaluation.EvaluationAssert.assertEvaluation
import org.junit.Test

class HasAtLeastCertainAgeTest {

    @Test
    fun canEvaluate() {
        val function = HasAtLeastCertainAge(2020, 18)
        assertEvaluation(EvaluationResult.PASS, function.evaluate(GeneralTestFactory.withBirthYear(1960)))
        assertEvaluation(EvaluationResult.FAIL, function.evaluate(GeneralTestFactory.withBirthYear(2014)))
        assertEvaluation(EvaluationResult.UNDETERMINED, function.evaluate(GeneralTestFactory.withBirthYear(2002)))
    }
}