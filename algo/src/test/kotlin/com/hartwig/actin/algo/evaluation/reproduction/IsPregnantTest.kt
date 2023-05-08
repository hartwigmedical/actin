package com.hartwig.actin.algo.evaluation.reproduction

import com.hartwig.actin.algo.datamodel.EvaluationResult
import com.hartwig.actin.algo.evaluation.EvaluationAssert.assertEvaluation
import com.hartwig.actin.clinical.datamodel.Gender
import org.junit.Test

class IsPregnantTest {
    @Test
    fun canEvaluate() {
        val function = IsPregnant()
        assertEvaluation(EvaluationResult.NOT_EVALUATED, function.evaluate(ReproductionTestFactory.withGender(Gender.FEMALE)))
        assertEvaluation(EvaluationResult.FAIL, function.evaluate(ReproductionTestFactory.withGender(Gender.MALE)))
    }
}