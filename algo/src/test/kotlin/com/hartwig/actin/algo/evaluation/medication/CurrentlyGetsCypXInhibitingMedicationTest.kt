package com.hartwig.actin.algo.evaluation.medication

import com.hartwig.actin.algo.datamodel.EvaluationResult
import com.hartwig.actin.algo.evaluation.EvaluationAssert.assertEvaluation
import com.hartwig.actin.clinical.datamodel.CypInteraction
import org.junit.Test

private const val TARGET_CYP = "9A9"

class CurrentlyGetsCypXInhibitingMedicationTest {
    private val alwaysActiveFunction = CurrentlyGetsCypXInhibitingMedication(MedicationTestFactory.alwaysActive(), TARGET_CYP)
    private val alwaysPlannedFunction = CurrentlyGetsCypXInhibitingMedication(MedicationTestFactory.alwaysPlanned(), TARGET_CYP)

    @Test
    fun `Should pass when CYP inhibiting medication`() {
        assertEvaluation(
            EvaluationResult.PASS, alwaysActiveFunction.evaluate(
                MedicationTestFactory.withCypInteraction(TARGET_CYP, CypInteraction.Type.INHIBITOR, CypInteraction.Strength.STRONG)
            )
        )
    }

    @Test
    fun `Should fail when CYP inhibiting medication that does not match CYP`() {
        assertEvaluation(
            EvaluationResult.FAIL, alwaysActiveFunction.evaluate(
                MedicationTestFactory.withCypInteraction("3A4", CypInteraction.Type.INHIBITOR, CypInteraction.Strength.STRONG)
            )
        )
    }

    @Test
    fun `Should fail when no CYP inhibiting medication`() {
        assertEvaluation(
            EvaluationResult.FAIL, alwaysActiveFunction.evaluate(
                MedicationTestFactory.withCypInteraction(TARGET_CYP, CypInteraction.Type.SUBSTRATE, CypInteraction.Strength.STRONG)
            )
        )
    }

    @Test
    fun `Should fail when patient uses no medication`() {
        assertEvaluation(EvaluationResult.FAIL, alwaysActiveFunction.evaluate(MedicationTestFactory.withMedications(emptyList())))
    }

    @Test
    fun `Should warn when patient plans to use CYP inhibiting medication`() {
        assertEvaluation(
            EvaluationResult.WARN, alwaysPlannedFunction.evaluate(
                MedicationTestFactory.withCypInteraction(TARGET_CYP, CypInteraction.Type.INHIBITOR, CypInteraction.Strength.STRONG)
            )
        )
    }

    @Test
    fun `Should fail when patient plans to use medication which is not CYP inhibiting`() {
        assertEvaluation(
            EvaluationResult.FAIL, alwaysPlannedFunction.evaluate(
                MedicationTestFactory.withCypInteraction(TARGET_CYP, CypInteraction.Type.SUBSTRATE, CypInteraction.Strength.STRONG)
            )
        )
    }
}