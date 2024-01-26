package com.hartwig.actin.algo.evaluation.medication

import com.hartwig.actin.algo.datamodel.EvaluationResult
import com.hartwig.actin.algo.evaluation.EvaluationAssert.assertEvaluation
import org.junit.Test

class CurrentlyGetsMedicationOfNameTest {
    private val alwaysActiveFunction = CurrentlyGetsMedicationOfName(MedicationTestFactory.alwaysActive(), setOf("term 1"))
    private val alwaysPlannedFunction = CurrentlyGetsMedicationOfName(MedicationTestFactory.alwaysPlanned(), setOf("term 1"))

    @Test
    fun `Should fail when patient uses no medications`() {
        assertEvaluation(EvaluationResult.FAIL, alwaysActiveFunction.evaluate(MedicationTestFactory.withMedications(emptyList())))
    }

    @Test
    fun `Should fail when patient uses medication with wrong name`() {
        assertEvaluation(
            EvaluationResult.FAIL, alwaysActiveFunction.evaluate(
                MedicationTestFactory.withMedications(listOf(MedicationTestFactory.medication("This is Term 2")))
            )
        )
    }

    @Test
    fun `Should pass when patient uses medication with correct name`() {
        assertEvaluation(
            EvaluationResult.PASS, alwaysActiveFunction.evaluate(
                MedicationTestFactory.withMedications(listOf(MedicationTestFactory.medication("This is Term 1")))
            )
        )
    }

    @Test
    fun `Should warn when patient plans to use medication with correct name`() {
        assertEvaluation(
            EvaluationResult.WARN, alwaysPlannedFunction.evaluate(
                MedicationTestFactory.withMedications(listOf(MedicationTestFactory.medication("This is Term 1")))
            )
        )
    }

    @Test
    fun `Should fail when patient plans to use medication with wrong name`() {
        assertEvaluation(
            EvaluationResult.FAIL, alwaysPlannedFunction.evaluate(
                MedicationTestFactory.withMedications(listOf(MedicationTestFactory.medication("This is Term 2")))
            )
        )
    }
}