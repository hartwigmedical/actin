package com.hartwig.actin.algo.evaluation.medication

import com.hartwig.actin.algo.evaluation.EvaluationAssert.assertEvaluation
import com.hartwig.actin.datamodel.TestPatientFactory
import com.hartwig.actin.datamodel.algo.EvaluationResult
import com.hartwig.actin.datamodel.clinical.CypInteraction
import org.assertj.core.api.Assertions.assertThat
import org.junit.Test

class CurrentlyGetsAnyCypInducingMedicationTest {
    private val alwaysActiveFunction = CurrentlyGetsAnyCypInducingMedication(MedicationTestFactory.alwaysActive())
    private val alwaysPlannedFunction = CurrentlyGetsAnyCypInducingMedication(MedicationTestFactory.alwaysPlanned())
    private val patientWithCypInducingMedication =
        MedicationTestFactory.withCypInteraction("9A9", CypInteraction.Type.INDUCER, CypInteraction.Strength.STRONG)
    private val patientWithCypSubstrateMedication =
        MedicationTestFactory.withCypInteraction("9A9", CypInteraction.Type.SUBSTRATE, CypInteraction.Strength.STRONG)

    @Test
    fun `Should pass when any CYP-inducing medication`() {
        assertEvaluation(EvaluationResult.PASS, alwaysActiveFunction.evaluate(patientWithCypInducingMedication))
    }

    @Test
    fun `Should fail when no CYP-inducing medication`() {
        assertEvaluation(EvaluationResult.FAIL, alwaysActiveFunction.evaluate(patientWithCypSubstrateMedication))
    }

    @Test
    fun `Should fail when patient uses no medication`() {
        assertEvaluation(EvaluationResult.FAIL, alwaysActiveFunction.evaluate(MedicationTestFactory.withMedications(emptyList())))
    }

    @Test
    fun `Should warn when patient plans to use CYP inducing medication`() {
        assertEvaluation(EvaluationResult.WARN, alwaysPlannedFunction.evaluate(patientWithCypInducingMedication))
    }

    @Test
    fun `Should fail when patient plans to medication which is not CYP inducing`() {
        assertEvaluation(EvaluationResult.FAIL, alwaysPlannedFunction.evaluate(patientWithCypSubstrateMedication))
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