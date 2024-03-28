package com.hartwig.actin.algo.evaluation.othercondition

import com.hartwig.actin.TestPatientFactory
import com.hartwig.actin.algo.datamodel.EvaluationResult
import com.hartwig.actin.algo.doid.DoidConstants
import com.hartwig.actin.algo.evaluation.EvaluationAssert.assertEvaluation
import com.hartwig.actin.algo.evaluation.othercondition.OtherConditionTestFactory.complication
import com.hartwig.actin.algo.evaluation.othercondition.OtherConditionTestFactory.intolerance
import com.hartwig.actin.algo.evaluation.othercondition.OtherConditionTestFactory.priorOtherCondition
import com.hartwig.actin.algo.evaluation.othercondition.OtherConditionTestFactory.withIntolerances
import com.hartwig.actin.algo.evaluation.othercondition.OtherConditionTestFactory.withPriorOtherCondition
import com.hartwig.actin.algo.evaluation.othercondition.OtherConditionTestFactory.withPriorOtherConditions
import com.hartwig.actin.clinical.datamodel.TestMedicationFactory
import com.hartwig.actin.doid.TestDoidModelFactory
import org.assertj.core.api.Assertions.assertThat
import org.junit.Test

class HasContraindicationToCTTest {
    private val function = HasContraindicationToCT(TestDoidModelFactory.createMinimalTestDoidModel())

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
                        priorOtherCondition(doids = setOf("wrong doid")),
                        priorOtherCondition(name = "not a contraindication")
                    )
                )
            )
        )
    }

    @Test
    fun `Should pass with a condition with correct DOID`() {
        assertEvaluation(
            EvaluationResult.PASS,
            function.evaluate(withPriorOtherCondition(priorOtherCondition(doids = setOf(DoidConstants.KIDNEY_DISEASE_DOID))))
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
    fun `Should fail with no relevant medication`() {
        val medications = listOf(TestMedicationFactory.createMinimal().copy(name = "no relevant medication"))
        assertEvaluation(EvaluationResult.FAIL, function.evaluate(OtherConditionTestFactory.withMedications(medications)))
    }

    @Test
    fun `Should pass with relevant medication`() {
        val relevantMedication: String = HasContraindicationToCT.MEDICATIONS_BEING_CONTRAINDICATIONS_TO_CT.first()
        val medications = listOf(TestMedicationFactory.createMinimal().copy(name = relevantMedication))
        assertEvaluation(EvaluationResult.PASS, function.evaluate(OtherConditionTestFactory.withMedications(medications)))
    }

    @Test
    fun `Should fail without complications`() {
        assertEvaluation(EvaluationResult.FAIL, function.evaluate(OtherConditionTestFactory.withComplications(emptyList())))
    }

    @Test
    fun `Should fail with no relevant complication`() {
        val complications = listOf(complication("no relevant complication"))
        assertEvaluation(EvaluationResult.FAIL, function.evaluate(OtherConditionTestFactory.withComplications(complications)))
    }

    @Test
    fun `Should pass with relevant complication`() {
        val complications = listOf(complication(HasContraindicationToCT.COMPLICATIONS_BEING_CONTRAINDICATIONS_TO_CT.first()))
        assertEvaluation(EvaluationResult.PASS, function.evaluate(OtherConditionTestFactory.withComplications(complications)))
    }

    @Test
    fun `Should be undetermined if medication is not provided`() {
        val result = function.evaluate(
            TestPatientFactory.createMinimalTestPatientRecord().copy(medications = null)
        )
        assertEvaluation(EvaluationResult.UNDETERMINED, result)
        assertThat(result.recoverable).isTrue()
    }
}