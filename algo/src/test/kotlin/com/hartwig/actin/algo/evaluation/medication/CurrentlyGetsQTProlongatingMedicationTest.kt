package com.hartwig.actin.algo.evaluation.medication

import com.hartwig.actin.algo.datamodel.EvaluationResult
import com.hartwig.actin.algo.evaluation.EvaluationAssert.assertEvaluation
import com.hartwig.actin.clinical.datamodel.Medication
import com.hartwig.actin.clinical.datamodel.QTProlongatingRisk
import com.hartwig.actin.clinical.datamodel.TestMedicationFactory
import org.junit.Test

class CurrentlyGetsQTProlongatingMedicationTest {
    @Test
    fun shouldPassWhenPatientUsesKnownQTProlongatingMedication() {
        val medications = listOf(TestMedicationFactory.builder().qtProlongatingRisk(QTProlongatingRisk.KNOWN).build())
        assertEvaluation(EvaluationResult.PASS, FUNCTION.evaluate(MedicationTestFactory.withMedications(medications)))
    }

    @Test
    fun shouldFailWhenPatientDoesNotUseQTProlongatingMedication() {
        val medications = listOf(TestMedicationFactory.builder().qtProlongatingRisk(QTProlongatingRisk.NONE).build())
        assertEvaluation(EvaluationResult.FAIL, FUNCTION.evaluate(MedicationTestFactory.withMedications(medications)))

    }

    @Test
    fun shouldFailWhenPatientUsesNoMedication() {
        val medications = emptyList<Medication>()
        assertEvaluation(EvaluationResult.FAIL, FUNCTION.evaluate(MedicationTestFactory.withMedications(medications)))
    }

    companion object {
        private val FUNCTION = CurrentlyGetsQTProlongatingMedication(MedicationTestFactory.alwaysActive())
    }
}