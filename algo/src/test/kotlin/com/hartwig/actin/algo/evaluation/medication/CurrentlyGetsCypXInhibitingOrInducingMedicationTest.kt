package com.hartwig.actin.algo.evaluation.medication

import com.hartwig.actin.algo.evaluation.EvaluationAssert.assertEvaluation
import com.hartwig.actin.datamodel.TestPatientFactory
import com.hartwig.actin.datamodel.algo.EvaluationResult
import com.hartwig.actin.datamodel.clinical.DrugInteraction
import org.assertj.core.api.Assertions.assertThat
import org.junit.jupiter.api.Test

private const val TARGET_CYP = "9A9"

class CurrentlyGetsCypXInhibitingOrInducingMedicationTest {
    private val alwaysActiveFunction = CurrentlyGetsCypXInhibitingOrInducingMedication(MedicationTestFactory.alwaysActive(), TARGET_CYP)
    private val alwaysPlannedFunction = CurrentlyGetsCypXInhibitingOrInducingMedication(MedicationTestFactory.alwaysPlanned(), TARGET_CYP)

    @Test
    fun `Should pass when CYP inhibiting or inducing medication`() {
        assertEvaluation(
            EvaluationResult.PASS, alwaysActiveFunction.evaluate(
                MedicationTestFactory.withCypInteraction(TARGET_CYP, DrugInteraction.Type.INDUCER, DrugInteraction.Strength.STRONG, "name")
            ),
            "Active CYP9A9 inhibiting or inducing medication in provided medications (name)"
        )
        assertEvaluation(
            EvaluationResult.PASS, alwaysActiveFunction.evaluate(
                MedicationTestFactory.withCypInteraction(TARGET_CYP, DrugInteraction.Type.INHIBITOR, DrugInteraction.Strength.STRONG, "name")
            ),
            "Active CYP9A9 inhibiting or inducing medication in provided medications (name)"
        )
    }

    @Test
    fun `Should fail with CYP inhibiting or inducing medication that does not match CYP`() {
        assertEvaluation(
            EvaluationResult.FAIL, alwaysActiveFunction.evaluate(
                MedicationTestFactory.withCypInteraction(
                    "3A4", DrugInteraction.Type.INDUCER, DrugInteraction.Strength.STRONG
                )
            ),
            "No active CYP9A9 inhibiting or inducing medication in provided medications"
        )
        assertEvaluation(
            EvaluationResult.FAIL, alwaysActiveFunction.evaluate(
                MedicationTestFactory.withCypInteraction("3A4", DrugInteraction.Type.INHIBITOR, DrugInteraction.Strength.STRONG)
            ),
            "No active CYP9A9 inhibiting or inducing medication in provided medications"
        )
    }

    @Test
    fun `Should fail when no CYP inhibiting or inducing medication`() {
        assertEvaluation(
            EvaluationResult.FAIL, alwaysActiveFunction.evaluate(
                MedicationTestFactory.withCypInteraction(TARGET_CYP, DrugInteraction.Type.SUBSTRATE, DrugInteraction.Strength.STRONG)
            ),
            "No active CYP9A9 inhibiting or inducing medication in provided medications"
        )
    }

    @Test
    fun `Should fail when patient uses no medication`() {
        assertEvaluation(
            EvaluationResult.FAIL,
            alwaysActiveFunction.evaluate(MedicationTestFactory.withMedications(emptyList())),
            "No active CYP9A9 inhibiting or inducing medication in provided medications"
        )
    }

    @Test
    fun `Should warn when patient plans to use CYP inhibiting or inducing medication`() {
        assertEvaluation(
            EvaluationResult.WARN, alwaysPlannedFunction.evaluate(
                MedicationTestFactory.withCypInteraction(TARGET_CYP, DrugInteraction.Type.INDUCER, DrugInteraction.Strength.STRONG, "name")
            ),
            "Planned CYP9A9 inhibiting or inducing medication in provided medications (name)"
        )
        assertEvaluation(
            EvaluationResult.WARN, alwaysPlannedFunction.evaluate(
                MedicationTestFactory.withCypInteraction(TARGET_CYP, DrugInteraction.Type.INHIBITOR, DrugInteraction.Strength.STRONG, "name")
            ),
            "Planned CYP9A9 inhibiting or inducing medication in provided medications (name)"
        )
    }

    @Test
    fun `Should fail when patient plans to use medication which is not CYP inhibiting or inducing`() {
        assertEvaluation(
            EvaluationResult.FAIL, alwaysPlannedFunction.evaluate(
                MedicationTestFactory.withCypInteraction(TARGET_CYP, DrugInteraction.Type.SUBSTRATE, DrugInteraction.Strength.STRONG)
            ),
            "No active CYP9A9 inhibiting or inducing medication in provided medications"
        )
    }

    @Test
    fun `Should be undetermined if medication is not provided`() {
        val medicationNotProvided = TestPatientFactory.createMinimalTestWGSPatientRecord().copy(medications = null)
        val alwaysPlannedResult = alwaysPlannedFunction.evaluate(medicationNotProvided)
        assertEvaluation(EvaluationResult.UNDETERMINED, alwaysPlannedResult, "No medication data provided")
        assertThat(alwaysPlannedResult.recoverable).isTrue()
        val alwaysActiveResult = alwaysActiveFunction.evaluate(medicationNotProvided)
        assertEvaluation(EvaluationResult.UNDETERMINED, alwaysActiveResult, "No medication data provided")
        assertThat(alwaysActiveResult.recoverable).isTrue()
    }
}