package com.hartwig.actin.algo.evaluation.medication

import com.hartwig.actin.algo.datamodel.EvaluationResult
import com.hartwig.actin.algo.evaluation.EvaluationAssert.assertEvaluation
import com.hartwig.actin.clinical.datamodel.Medication
import com.hartwig.actin.clinical.datamodel.TestMedicationFactory
import org.junit.Test

class CurrentlyGetsMedicationOfNameTest {
    @Test
    fun canEvaluate() {
        val function = CurrentlyGetsMedicationOfName(MedicationTestFactory.alwaysActive(), setOf("term 1"))

        // No medications yet
        val medications: MutableList<Medication> = mutableListOf()
        assertEvaluation(EvaluationResult.FAIL, function.evaluate(MedicationTestFactory.withMedications(medications)))

        // Medication with wrong name
        medications.add(TestMedicationFactory.builder().name("This is Term 2").build())
        assertEvaluation(EvaluationResult.FAIL, function.evaluate(MedicationTestFactory.withMedications(medications)))

        // Medication with right name
        medications.add(TestMedicationFactory.builder().name("This is Term 1").build())
        assertEvaluation(EvaluationResult.PASS, function.evaluate(MedicationTestFactory.withMedications(medications)))
    }
}