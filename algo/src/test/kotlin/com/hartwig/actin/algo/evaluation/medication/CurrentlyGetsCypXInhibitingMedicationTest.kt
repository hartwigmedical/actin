package com.hartwig.actin.algo.evaluation.medication

import com.hartwig.actin.algo.datamodel.EvaluationResult
import com.hartwig.actin.algo.evaluation.EvaluationAssert.assertEvaluation
import com.hartwig.actin.clinical.datamodel.CypInteraction
import org.junit.Test

private const val TARGET_CYP = "9A9"

class CurrentlyGetsCypXInhibitingMedicationTest {
    private val function = CurrentlyGetsCypXInhibitingMedication(MedicationTestFactory.alwaysActive(), TARGET_CYP)
    
    @Test
    fun `Should pass when CYP inhibiting medication`() {
        assertEvaluation(
            EvaluationResult.PASS, function.evaluate(
                MedicationTestFactory.withCypInteraction(
                    TARGET_CYP, CypInteraction.Type.INHIBITOR, CypInteraction.Strength.STRONG
                )
            )
        )
    }

    @Test
    fun `Should fail when CYP inhibiting medication that does not match CYP`() {
        assertEvaluation(
            EvaluationResult.FAIL, function.evaluate(
                MedicationTestFactory.withCypInteraction(
                    "3A4", CypInteraction.Type.INHIBITOR, CypInteraction.Strength.STRONG

                )
            )
        )
    }

    @Test
    fun `Should fail when no CYP inhibiting medication`() {
        assertEvaluation(
            EvaluationResult.FAIL, function.evaluate(
                MedicationTestFactory.withCypInteraction(
                    TARGET_CYP, CypInteraction.Type.SUBSTRATE, CypInteraction.Strength.STRONG
                )
            )
        )
    }

    @Test
    fun `Should fail when patient uses no medication`() {
        assertEvaluation(EvaluationResult.FAIL, function.evaluate(MedicationTestFactory.withMedications(emptyList())))
    }
}