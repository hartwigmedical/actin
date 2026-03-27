package com.hartwig.actin.algo.evaluation.general

import com.hartwig.actin.algo.evaluation.EvaluationAssert.assertEvaluation
import com.hartwig.actin.datamodel.algo.EvaluationResult
import com.hartwig.actin.datamodel.clinical.Gender
import org.junit.jupiter.api.Test

class IsFemaleTest {

    private val function = IsFemale()

    @Test
    fun `Should fail if patient is male`() {
        assertEvaluation(EvaluationResult.FAIL, function.evaluate(GeneralTestFactory.withGender(Gender.MALE)))
    }

    @Test
    fun `Should pass if patient is female`() {
        assertEvaluation(EvaluationResult.PASS, function.evaluate(GeneralTestFactory.withGender(Gender.FEMALE)))
    }

    @Test
    fun `Should be undetermined if patient gender is unknown`() {
        assertEvaluation(EvaluationResult.UNDETERMINED, function.evaluate(GeneralTestFactory.withGender(null)))
    }
}