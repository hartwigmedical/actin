package com.hartwig.actin.algo.evaluation.medication

import com.hartwig.actin.algo.datamodel.EvaluationResult
import com.hartwig.actin.algo.evaluation.EvaluationAssert.assertEvaluation
import org.junit.Test

class CurrentlyGetsMedicationOfNameTest {
    private val alwaysActiveFunction = CurrentlyGetsMedicationOfName(MedicationTestFactory.alwaysActive(), setOf("term 1"), false)
    private val alwaysPlannedFunction = CurrentlyGetsMedicationOfName(MedicationTestFactory.alwaysPlanned(), setOf("term 1"), false)

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

    @Test
    fun `Should only consider systemic drugs when isSystemic is true`() {
        val function = CurrentlyGetsMedicationOfName(MedicationTestFactory.alwaysActive(), setOf("term 1"), true)
        val systemicDrug = listOf(
            MedicationTestFactory.medication(
                "This is Term 1", administrationRoute = MedicationSelector.SYSTEMIC_ADMINISTRATION_ROUTE_SET.iterator().next()
            )
        )
        assertEvaluation(EvaluationResult.PASS, function.evaluate(MedicationTestFactory.withMedications(systemicDrug)))

        val nonSystemicDrug = listOf(MedicationTestFactory.medication("This is Term 1", administrationRoute = "non-systemic"))
        assertEvaluation(EvaluationResult.FAIL, function.evaluate(MedicationTestFactory.withMedications(nonSystemicDrug)))
    }
}