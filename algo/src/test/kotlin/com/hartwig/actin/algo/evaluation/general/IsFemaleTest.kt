package com.hartwig.actin.algo.evaluation.general

import com.hartwig.actin.algo.evaluation.EvaluationAssert
import com.hartwig.actin.datamodel.algo.EvaluationResult
import com.hartwig.actin.datamodel.clinical.Gender
import org.junit.Test

class IsFemaleTest {

    @Test
    fun canEvaluate() {
        val function = IsFemale()
        EvaluationAssert.assertEvaluation(EvaluationResult.FAIL, function.evaluate(GeneralTestFactory.withGender(Gender.MALE)))
        EvaluationAssert.assertEvaluation(EvaluationResult.PASS, function.evaluate(GeneralTestFactory.withGender(Gender.FEMALE)))
    }
}