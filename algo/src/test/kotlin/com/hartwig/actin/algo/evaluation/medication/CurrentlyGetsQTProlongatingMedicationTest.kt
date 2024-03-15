package com.hartwig.actin.algo.evaluation.medication

import com.hartwig.actin.algo.datamodel.EvaluationResult
import com.hartwig.actin.algo.evaluation.EvaluationAssert.assertEvaluation
import com.hartwig.actin.clinical.datamodel.QTProlongatingRisk
import com.hartwig.actin.clinical.datamodel.TestMedicationFactory
import org.junit.Test

class CurrentlyGetsQTProlongatingMedicationTest {

    private val alwaysActiveFunction = CurrentlyGetsQTProlongatingMedication(MedicationTestFactory.alwaysActive())
    private val alwaysPlannedFunction = CurrentlyGetsQTProlongatingMedication(MedicationTestFactory.alwaysPlanned())

    @Test
    fun `Should pass when patient uses known QT prolongating medication`() {
        val medications = listOf(MedicationTestFactory.medication(qtProlongatingRisk = QTProlongatingRisk.KNOWN))
        assertEvaluation(EvaluationResult.PASS, alwaysActiveFunction.evaluate(MedicationTestFactory.withMedications(medications)))
    }

    @Test
    fun `Should fail when patient does not use QT prolongating medication`() {
        val medications = listOf(TestMedicationFactory.createMinimal())
        assertEvaluation(EvaluationResult.FAIL, alwaysActiveFunction.evaluate(MedicationTestFactory.withMedications(medications)))
    }

    @Test
    fun `Should fail when patient uses no medication`() {
        assertEvaluation(EvaluationResult.FAIL, alwaysActiveFunction.evaluate(MedicationTestFactory.withMedications(emptyList())))
    }

    @Test
    fun `Should warn when patient plans to use known QT prolongating medication`() {
        val medications = listOf(MedicationTestFactory.medication(qtProlongatingRisk = QTProlongatingRisk.KNOWN))
        assertEvaluation(EvaluationResult.WARN, alwaysPlannedFunction.evaluate(MedicationTestFactory.withMedications(medications)))
    }

    @Test
    fun `Should fail when patient plans to use medication that is not QT prolongating`() {
        val medications = listOf(TestMedicationFactory.createMinimal())
        assertEvaluation(EvaluationResult.FAIL, alwaysPlannedFunction.evaluate(MedicationTestFactory.withMedications(medications)))
    }

    @Test
    fun `Should fail when patient uses medication with unknown QT-prolongating status`(){
        val medications = listOf(MedicationTestFactory.medication(qtProlongatingRisk = QTProlongatingRisk.UNKNOWN))
        assertEvaluation(EvaluationResult.FAIL, alwaysActiveFunction.evaluate(MedicationTestFactory.withMedications(medications)))
    }
}