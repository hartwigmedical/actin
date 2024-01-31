package com.hartwig.actin.algo.evaluation.general

import com.hartwig.actin.algo.datamodel.EvaluationResult
import com.hartwig.actin.algo.evaluation.EvaluationAssert
import com.hartwig.actin.clinical.datamodel.Gender
import org.junit.Test

class IsMaleTest {

    @Test
    fun canEvaluate() {
        val function = IsMale()
        EvaluationAssert.assertEvaluation(EvaluationResult.PASS, function.evaluate(GeneralTestFactory.withGender(Gender.MALE)))
        EvaluationAssert.assertEvaluation(EvaluationResult.FAIL, function.evaluate(GeneralTestFactory.withGender(Gender.FEMALE)))
    }
}