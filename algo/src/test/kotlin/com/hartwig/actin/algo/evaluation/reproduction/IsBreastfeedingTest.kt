package com.hartwig.actin.algo.evaluation.reproduction

import com.hartwig.actin.algo.evaluation.EvaluationAssert.assertEvaluation
import com.hartwig.actin.datamodel.algo.EvaluationResult
import com.hartwig.actin.datamodel.clinical.Gender
import org.junit.Test

class IsBreastfeedingTest {
    @Test
    fun canEvaluate() {
        val function = IsBreastfeeding()
        assertEvaluation(EvaluationResult.NOT_EVALUATED, function.evaluate(ReproductionTestFactory.withGender(Gender.FEMALE)))
        assertEvaluation(EvaluationResult.FAIL, function.evaluate(ReproductionTestFactory.withGender(Gender.MALE)))
    }
}