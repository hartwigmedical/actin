package com.hartwig.actin.algo.evaluation.medication

import com.hartwig.actin.algo.evaluation.EvaluationAssert.assertEvaluation
import com.hartwig.actin.datamodel.TestPatientFactory
import com.hartwig.actin.datamodel.algo.EvaluationResult
import com.hartwig.actin.datamodel.clinical.DrugInteraction
import org.assertj.core.api.Assertions.assertThat
import org.junit.Test

private const val TARGET_CYP = "9A9"

class CurrentlyGetsCypXInhibitingOrInducingMedicationTest {
    private val alwaysActiveFunction = CurrentlyGetsCypXInhibitingOrInducingMedication(MedicationTestFactory.alwaysActive(), TARGET_CYP)
    private val alwaysPlannedFunction = CurrentlyGetsCypXInhibitingOrInducingMedication(MedicationTestFactory.alwaysPlanned(), TARGET_CYP)

    @Test
    fun `Should pass when CYP inhibiting or inducing medication`() {
        assertEvaluation(
            EvaluationResult.PASS, alwaysActiveFunction.evaluate(
                MedicationTestFactory.withCypInteraction(TARGET_CYP, DrugInteraction.Type.INDUCER, DrugInteraction.Strength.STRONG)
            )
        )
        assertEvaluation(
            EvaluationResult.PASS, alwaysActiveFunction.evaluate(
                MedicationTestFactory.withCypInteraction(TARGET_CYP, DrugInteraction.Type.INHIBITOR, DrugInteraction.Strength.STRONG)
            )
        )
    }

    @Test
    fun `Should fail with CYP inhibiting or inducing medication that does not match CYP`() {
        assertEvaluation(
            EvaluationResult.FAIL, alwaysActiveFunction.evaluate(
                MedicationTestFactory.withCypInteraction(
                    "3A4", DrugInteraction.Type.INDUCER, DrugInteraction.Strength.STRONG
                )
            )
        )
        assertEvaluation(
            EvaluationResult.FAIL, alwaysActiveFunction.evaluate(
                MedicationTestFactory.withCypInteraction("3A4", DrugInteraction.Type.INHIBITOR, DrugInteraction.Strength.STRONG)
            )
        )
    }

    @Test
    fun `Should fail when no CYP inhibiting or inducing medication`() {
        assertEvaluation(
            EvaluationResult.FAIL, alwaysActiveFunction.evaluate(
                MedicationTestFactory.withCypInteraction(TARGET_CYP, DrugInteraction.Type.SUBSTRATE, DrugInteraction.Strength.STRONG)
            )
        )
    }

    @Test
    fun `Should fail when patient uses no medication`() {
        assertEvaluation(EvaluationResult.FAIL, alwaysActiveFunction.evaluate(MedicationTestFactory.withMedications(emptyList())))
    }

    @Test
    fun `Should warn when patient plans to use CYP inhibiting or inducing medication`() {
        assertEvaluation(
            EvaluationResult.WARN, alwaysPlannedFunction.evaluate(
                MedicationTestFactory.withCypInteraction(TARGET_CYP, DrugInteraction.Type.INDUCER, DrugInteraction.Strength.STRONG)
            )
        )
        assertEvaluation(
            EvaluationResult.WARN, alwaysPlannedFunction.evaluate(
                MedicationTestFactory.withCypInteraction(TARGET_CYP, DrugInteraction.Type.INHIBITOR, DrugInteraction.Strength.STRONG)
            )
        )
    }

    @Test
    fun `Should fail when patient plans to use medication which is not CYP inhibiting or inducing`() {
        assertEvaluation(
            EvaluationResult.FAIL, alwaysPlannedFunction.evaluate(
                MedicationTestFactory.withCypInteraction(TARGET_CYP, DrugInteraction.Type.SUBSTRATE, DrugInteraction.Strength.STRONG)
            )
        )
    }

    @Test
    fun `Should be undetermined if medication is not provided`() {
        val medicationNotProvided = TestPatientFactory.createMinimalTestWGSPatientRecord().copy(medications = null)
        val alwaysPlannedResult = alwaysPlannedFunction.evaluate(medicationNotProvided)
        assertEvaluation(EvaluationResult.UNDETERMINED, alwaysPlannedResult)
        assertThat(alwaysPlannedResult.recoverable).isTrue()
        val alwaysActiveResult = alwaysActiveFunction.evaluate(medicationNotProvided)
        assertEvaluation(EvaluationResult.UNDETERMINED, alwaysActiveResult)
        assertThat(alwaysActiveResult.recoverable).isTrue()
    }
}