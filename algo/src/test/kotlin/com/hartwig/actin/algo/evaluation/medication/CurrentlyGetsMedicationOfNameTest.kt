package com.hartwig.actin.algo.evaluation.medication

import com.hartwig.actin.algo.datamodel.EvaluationResult
import com.hartwig.actin.algo.evaluation.EvaluationAssert.assertEvaluation
import com.hartwig.actin.clinical.datamodel.Medication
import com.hartwig.actin.clinical.datamodel.TestMedicationFactory
import org.junit.Test

class CurrentlyGetsMedicationOfNameTest {

    private val alwaysActiveFunction = CurrentlyGetsMedicationOfName(MedicationTestFactory.alwaysActive(), setOf("term 1"))
    private val alwaysPlannedFunction = CurrentlyGetsMedicationOfName(MedicationTestFactory.alwaysPlanned(), setOf("term 1"))

    @Test
    fun `Should fail when patient uses no medication`() {
        val medications = emptyList<Medication>()
        assertEvaluation(EvaluationResult.FAIL, alwaysActiveFunction.evaluate(MedicationTestFactory.withMedications(medications)))
    }

    @Test
    fun `Should fail when patient uses medication with wrong name`() {
        val medications = listOf(TestMedicationFactory.builder().name("This is Term 2").build())
        assertEvaluation(EvaluationResult.FAIL, alwaysActiveFunction.evaluate(MedicationTestFactory.withMedications(medications)))
    }

    @Test
    fun `Should pass when patient uses medication with correct name`() {
        val medications = listOf(TestMedicationFactory.builder().name("This is Term 1").build())
        assertEvaluation(EvaluationResult.PASS, alwaysActiveFunction.evaluate(MedicationTestFactory.withMedications(medications)))
    }

    @Test
    fun `Should warn when patient plans to use medication with correct name`() {
        val medications = listOf(TestMedicationFactory.builder().name("This is Term 1").build())
        assertEvaluation(EvaluationResult.WARN, alwaysPlannedFunction.evaluate(MedicationTestFactory.withMedications(medications)))
    }

    @Test
    fun `Should fail when patient plans to use medication with wrong name`() {
        val medications = listOf(TestMedicationFactory.builder().name("This is Term 2").build())
        assertEvaluation(EvaluationResult.FAIL, alwaysPlannedFunction.evaluate(MedicationTestFactory.withMedications(medications)))
    }
}