package com.hartwig.actin.algo.evaluation.medication

import com.hartwig.actin.algo.datamodel.EvaluationResult
import com.hartwig.actin.algo.evaluation.EvaluationAssert.assertEvaluation
import com.hartwig.actin.clinical.datamodel.CypInteraction
import org.junit.Test

class CurrentlyGetsAnyCypInhibitingOrInducingMedicationTest {
    private val function = CurrentlyGetsAnyCypInhibitingOrInducingMedication(MedicationTestFactory.alwaysActive())
    
    @Test
    fun `Should pass when any CYP-inhibiting or -inducing medication`() {
        assertEvaluation(
            EvaluationResult.PASS, function.evaluate(
                MedicationTestFactory.withCypInteraction(
                    "9A9", CypInteraction.Type.INDUCER, CypInteraction.Strength.STRONG
                )
            )
        )
        assertEvaluation(
            EvaluationResult.PASS, function.evaluate(
                MedicationTestFactory.withCypInteraction(
                    "9A9", CypInteraction.Type.INHIBITOR, CypInteraction.Strength.STRONG
                )
            )
        )
    }

    @Test
    fun `Should fail when no CYP-inhibiting or -inducing medication`() {
        assertEvaluation(
            EvaluationResult.FAIL, function.evaluate(
                MedicationTestFactory.withCypInteraction(
                    "9A9", CypInteraction.Type.SUBSTRATE, CypInteraction.Strength.STRONG
                )
            )
        )
    }

    @Test
    fun `Should fail when patient uses no medication`() {
        assertEvaluation(EvaluationResult.FAIL, function.evaluate(MedicationTestFactory.withMedications(emptyList())))
    }
}