package com.hartwig.actin.algo.evaluation.othercondition

import com.hartwig.actin.algo.evaluation.EvaluationAssert.assertEvaluation
import com.hartwig.actin.algo.evaluation.othercondition.OtherConditionTestFactory.complication
import com.hartwig.actin.algo.evaluation.othercondition.OtherConditionTestFactory.intolerance
import com.hartwig.actin.algo.evaluation.othercondition.OtherConditionTestFactory.priorOtherCondition
import com.hartwig.actin.algo.evaluation.othercondition.OtherConditionTestFactory.withIntolerances
import com.hartwig.actin.algo.evaluation.othercondition.OtherConditionTestFactory.withPriorOtherCondition
import com.hartwig.actin.algo.evaluation.othercondition.OtherConditionTestFactory.withPriorOtherConditions
import com.hartwig.actin.algo.icd.IcdConstants
import com.hartwig.actin.datamodel.algo.EvaluationResult
import com.hartwig.actin.icd.TestIcdFactory
import org.junit.Test

class HasContraindicationToCTTest {
    private val function = HasContraindicationToCT(TestIcdFactory.createTestModel())
    private val correctCode = IcdConstants.KIDNEY_FAILURE_BLOCK

    @Test
    fun `Should fail with no prior other condition`() {
        assertEvaluation(EvaluationResult.FAIL, function.evaluate(withPriorOtherConditions(emptyList())))
    }

    @Test
    fun `Should fail with no relevant prior other condition`() {
        assertEvaluation(
            EvaluationResult.FAIL, function.evaluate(
                withPriorOtherConditions(
                    listOf(
                        priorOtherCondition(icdCode = "wrong code"),
                        priorOtherCondition(name = "not a contraindication")
                    )
                )
            )
        )
    }

    @Test
    fun `Should pass with a condition with correct ICD code`() {
        assertEvaluation(
            EvaluationResult.PASS,
            function.evaluate(withPriorOtherCondition(priorOtherCondition(icdCode = correctCode)))
        )
    }

    @Test
    fun `Should pass with a condition with correct name`() {
        val contraindicationName = HasContraindicationToCT.OTHER_CONDITIONS_BEING_CONTRAINDICATIONS_TO_CT.first()
        assertEvaluation(
            EvaluationResult.PASS, function.evaluate(withPriorOtherCondition(priorOtherCondition(name = contraindicationName)))
        )
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
    fun `Should pass with relevant intolerance`() {
        val relevantAllergy = HasContraindicationToCT.INTOLERANCES_BEING_CONTRAINDICATIONS_TO_CT.first()
        assertEvaluation(EvaluationResult.PASS, function.evaluate(withIntolerances(listOf(intolerance(relevantAllergy)))))
    }

    @Test
    fun `Should fail with no medications`() {
        assertEvaluation(EvaluationResult.FAIL, function.evaluate(OtherConditionTestFactory.withMedications(emptyList())))
    }

    @Test
    fun `Should fail without complications`() {
        assertEvaluation(EvaluationResult.FAIL, function.evaluate(OtherConditionTestFactory.withComplications(emptyList())))
    }

    @Test
    fun `Should fail with complication with wrong code`() {
        val complications = listOf(complication(icdCode = "wrong"))
        assertEvaluation(EvaluationResult.FAIL, function.evaluate(OtherConditionTestFactory.withComplications(complications)))
    }

    @Test
    fun `Should pass with complication with correct code`() {
        val complications = listOf(complication(icdCode = correctCode))
        assertEvaluation(EvaluationResult.PASS, function.evaluate(OtherConditionTestFactory.withComplications(complications)))
    }
}