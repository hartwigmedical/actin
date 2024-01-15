package com.hartwig.actin.algo.evaluation.medication

import com.hartwig.actin.algo.datamodel.EvaluationResult
import com.hartwig.actin.algo.evaluation.EvaluationAssert.assertEvaluation
import com.hartwig.actin.clinical.datamodel.QTProlongatingRisk
import com.hartwig.actin.clinical.datamodel.TestMedicationFactory
import org.junit.Test

class CurrentlyGetsQTProlongatingMedicationTest {
    private val function = CurrentlyGetsQTProlongatingMedication(MedicationTestFactory.alwaysActive())
    
    @Test
    fun `Should pass when patient uses known qTProlongating medication`() {
        val medications = listOf(MedicationTestFactory.medication(qtProlongatingRisk = QTProlongatingRisk.KNOWN))
        assertEvaluation(EvaluationResult.PASS, function.evaluate(MedicationTestFactory.withMedications(medications)))
    }

    @Test
    fun `Should fail when patient does not use qTProlongating medication`() {
        val medications = listOf(TestMedicationFactory.createMinimal())
        assertEvaluation(EvaluationResult.FAIL, function.evaluate(MedicationTestFactory.withMedications(medications)))

    }

    @Test
    fun `Should fail when patient uses no medication`() {
        assertEvaluation(EvaluationResult.FAIL, function.evaluate(MedicationTestFactory.withMedications(emptyList())))
    }
}