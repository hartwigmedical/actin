package com.hartwig.actin.algo.evaluation.othercondition

import com.hartwig.actin.algo.evaluation.EvaluationAssert.assertEvaluation
import com.hartwig.actin.algo.evaluation.othercondition.ComorbidityTestFactory.complication
import com.hartwig.actin.algo.evaluation.othercondition.ComorbidityTestFactory.intolerance
import com.hartwig.actin.algo.evaluation.othercondition.ComorbidityTestFactory.otherCondition
import com.hartwig.actin.algo.evaluation.othercondition.ComorbidityTestFactory.withIntolerances
import com.hartwig.actin.algo.evaluation.othercondition.ComorbidityTestFactory.withOtherCondition
import com.hartwig.actin.algo.evaluation.othercondition.ComorbidityTestFactory.withOtherConditions
import com.hartwig.actin.algo.icd.IcdConstants
import com.hartwig.actin.datamodel.algo.EvaluationResult
import com.hartwig.actin.icd.TestIcdFactory
import org.junit.Test

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
    fun `Should fail without complications`() {
        assertEvaluation(EvaluationResult.FAIL, function.evaluate(ComorbidityTestFactory.withComplications(emptyList())))
    }

    @Test
    fun `Should fail with complication with wrong code`() {
        val complications = listOf(complication(icdMainCode = "wrong"))
        assertEvaluation(EvaluationResult.FAIL, function.evaluate(ComorbidityTestFactory.withComplications(complications)))
    }

    @Test
    fun `Should pass with complication with correct code`() {
        val complications = listOf(complication(icdMainCode = correctCode))
        assertEvaluation(EvaluationResult.PASS, function.evaluate(ComorbidityTestFactory.withComplications(complications)))
    }
}