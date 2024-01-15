package com.hartwig.actin.algo.evaluation.medication

import com.hartwig.actin.algo.datamodel.EvaluationResult
import com.hartwig.actin.algo.evaluation.EvaluationAssert.assertEvaluation
import org.junit.Test

class CurrentlyGetsMedicationOfNameTest {
    private val function = CurrentlyGetsMedicationOfName(MedicationTestFactory.alwaysActive(), setOf("term 1"))
    
    @Test
    fun `Should fail with no medications`() {
        assertEvaluation(EvaluationResult.FAIL, function.evaluate(MedicationTestFactory.withMedications(emptyList())))
    }

    @Test
    fun `Should fail with wrong medication`() {
        assertEvaluation(
            EvaluationResult.FAIL, function.evaluate(
                MedicationTestFactory.withMedications(
                    listOf(
                        MedicationTestFactory.medication("This is Term 2")
                    )
                )
            )
        )
    }

    @Test
    fun `Should pass with matching medication`() {
        assertEvaluation(
            EvaluationResult.PASS, function.evaluate(
                MedicationTestFactory.withMedications(
                    listOf(
                        MedicationTestFactory.medication("This is Term 1")
                    )
                )
            )
        )
    }
}