package com.hartwig.actin.algo.evaluation.comorbidity

import com.hartwig.actin.algo.evaluation.EvaluationAssert.assertEvaluation
import com.hartwig.actin.algo.evaluation.comorbidity.ComorbidityTestFactory.intolerance
import com.hartwig.actin.algo.evaluation.comorbidity.ComorbidityTestFactory.otherCondition
import com.hartwig.actin.algo.evaluation.comorbidity.ComorbidityTestFactory.withIntolerances
import com.hartwig.actin.algo.evaluation.comorbidity.ComorbidityTestFactory.withOtherCondition
import com.hartwig.actin.algo.evaluation.comorbidity.ComorbidityTestFactory.withOtherConditions
import com.hartwig.actin.algo.icd.IcdConstants
import com.hartwig.actin.datamodel.algo.EvaluationResult
import com.hartwig.actin.icd.TestIcdFactory
import org.junit.jupiter.api.Test

class HasContraindicationToCTTest {
    private val function = HasContraindicationToCT(TestIcdFactory.createTestModel())
    private val correctCode = IcdConstants.KIDNEY_FAILURE_BLOCK

    @Test
    fun `Should fail with no other condition`() {
        assertEvaluation(EvaluationResult.FAIL, function.evaluate(withOtherConditions(emptyList())))
    }

    @Test
    fun `Should fail with no relevant other condition`() {
        assertEvaluation(
            EvaluationResult.FAIL, function.evaluate(
                withOtherConditions(
                    listOf(
                        otherCondition(icdMainCode = "wrong code"),
                        otherCondition(name = "not a contraindication")
                    )
                )
            )
        )
    }

    @Test
    fun `Should pass with a condition with correct ICD code`() {
        assertEvaluation(
            EvaluationResult.PASS,
            function.evaluate(withOtherCondition(otherCondition(icdMainCode = correctCode)))
        )
    }

    @Test
    fun `Should pass with other condition that matches by name`() {
        HasContraindicationToCT.COMORBIDITIES_THAT_ARE_CONTRAINDICATIONS_TO_CT.forEach { contraindicationName ->
            assertEvaluation(EvaluationResult.PASS, function.evaluate(withOtherCondition(otherCondition(name = contraindicationName))))
        }
    }

    @Test
    fun `Should fail with no intolerances`() {
        assertEvaluation(EvaluationResult.FAIL, function.evaluate(withIntolerances(emptyList())))
    }

    @Test
    fun `Should fail with no relevant intolerance`() {
        assertEvaluation(EvaluationResult.FAIL, function.evaluate(withIntolerances(listOf(intolerance("no relevant allergy")))))
    }

    @Test
    fun `Should pass with intolerance that matches by name`() {
        HasContraindicationToCT.COMORBIDITIES_THAT_ARE_CONTRAINDICATIONS_TO_CT.forEach { contraindicationName ->
            assertEvaluation(EvaluationResult.PASS, function.evaluate(withIntolerances(listOf(intolerance(contraindicationName)))))
        }
    }

    @Test
    fun `Should fail with no medications`() {
        assertEvaluation(EvaluationResult.FAIL, function.evaluate(ComorbidityTestFactory.withMedications(emptyList())))
    }

    @Test
    fun `Should fail with other condition provided in list with wrong code`() {
        assertEvaluation(EvaluationResult.FAIL, function.evaluate(withOtherCondition(otherCondition(icdMainCode = "wrong"))))
    }

    @Test
    fun `Should pass with other condition provided in list with correct code`() {
        assertEvaluation(EvaluationResult.PASS, function.evaluate(withOtherCondition(otherCondition(icdMainCode = correctCode))))
    }
}
