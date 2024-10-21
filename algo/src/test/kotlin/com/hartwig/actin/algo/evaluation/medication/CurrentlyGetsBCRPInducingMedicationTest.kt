package com.hartwig.actin.algo.evaluation.medication

import com.hartwig.actin.algo.evaluation.EvaluationAssert
import com.hartwig.actin.datamodel.algo.EvaluationResult
import org.junit.Test

class CurrentlyGetsBCRPInducingMedicationTest {

    private val function = CurrentlyGetsBCRPInducingMedication()

    @Test
    fun `Should evaluate to fail when patient uses no medication`() {
        EvaluationAssert.assertEvaluation(EvaluationResult.FAIL, function.evaluate(MedicationTestFactory.withMedications(emptyList())))
    }

    @Test
    fun `Should evaluate to undetermined when patient uses medication`() {
        EvaluationAssert.assertEvaluation(
            EvaluationResult.UNDETERMINED,
            function.evaluate(MedicationTestFactory.withMedications(listOf(MedicationTestFactory.medication())))
        )
    }
}