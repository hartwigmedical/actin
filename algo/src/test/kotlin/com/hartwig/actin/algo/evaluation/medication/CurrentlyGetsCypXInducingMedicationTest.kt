package com.hartwig.actin.algo.evaluation.medication

import com.hartwig.actin.TestPatientFactory
import com.hartwig.actin.algo.datamodel.EvaluationResult
import com.hartwig.actin.algo.evaluation.EvaluationAssert.assertEvaluation
import com.hartwig.actin.clinical.datamodel.CypInteraction
import org.assertj.core.api.Assertions.assertThat
import org.junit.Test

private const val TARGET_CYP = "9A9"

class CurrentlyGetsCypXInducingMedicationTest {
    private val alwaysActiveFunction = CurrentlyGetsCypXInducingMedication(MedicationTestFactory.alwaysActive(), TARGET_CYP)
    private val alwaysPlannedFunction = CurrentlyGetsCypXInducingMedication(MedicationTestFactory.alwaysPlanned(), TARGET_CYP)

    @Test
    fun `Should pass when CYP inducing medication`() {
        assertEvaluation(
            EvaluationResult.PASS, alwaysActiveFunction.evaluate(
                MedicationTestFactory.withCypInteraction(TARGET_CYP, CypInteraction.Type.INDUCER, CypInteraction.Strength.STRONG)
            )
        )
    }

    @Test
    fun `Should fail when CYP inducing medication that does not match CYP`() {
        assertEvaluation(
            EvaluationResult.FAIL, alwaysActiveFunction.evaluate(
                MedicationTestFactory.withCypInteraction("3A4", CypInteraction.Type.INDUCER, CypInteraction.Strength.STRONG)
            )
        )
    }

    @Test
    fun `Should fail when no CYP inducing medication`() {
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
    fun `Should warn when patient plans to use CYP inducing medication`() {
        assertEvaluation(
            EvaluationResult.WARN, alwaysPlannedFunction.evaluate(
                MedicationTestFactory.withCypInteraction(TARGET_CYP, CypInteraction.Type.INDUCER, CypInteraction.Strength.STRONG)
            )
        )
    }

    @Test
    fun `Should fail when patient plans to use CYP medication that does not match CYP`() {
        assertEvaluation(
            EvaluationResult.FAIL, alwaysPlannedFunction.evaluate(
                MedicationTestFactory.withCypInteraction("3A4", CypInteraction.Type.INDUCER, CypInteraction.Strength.STRONG)
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