package com.hartwig.actin.algo.evaluation.medication

import com.hartwig.actin.algo.datamodel.EvaluationResult
import com.hartwig.actin.algo.evaluation.EvaluationAssert.assertEvaluation
import com.hartwig.actin.clinical.datamodel.Medication
import com.hartwig.actin.clinical.datamodel.QTProlongatingRisk
import com.hartwig.actin.clinical.datamodel.TestMedicationFactory
import org.junit.Test

class CurrentlyGetsQTProlongatingMedicationTest {
    val function = CurrentlyGetsQTProlongatingMedication(MedicationTestFactory.alwaysActive())
    val medications: MutableList<Medication> = mutableListOf()

    @Test
    fun shouldPassWhenPatientUsesKnownQTProlongatingMedication() {
        medications.add(TestMedicationFactory.builder().qtProlongatingRisk(QTProlongatingRisk.KNOWN).build())
        assertEvaluation(EvaluationResult.PASS, function.evaluate(MedicationTestFactory.withMedications(medications)))
    }

    @Test
    fun shouldFailWhenPatientUsesNoQTProlongatingMedication() {
        medications.add(TestMedicationFactory.builder().qtProlongatingRisk(QTProlongatingRisk.NONE).build())
        assertEvaluation(EvaluationResult.FAIL, function.evaluate(MedicationTestFactory.withMedications(medications)))

    }
}