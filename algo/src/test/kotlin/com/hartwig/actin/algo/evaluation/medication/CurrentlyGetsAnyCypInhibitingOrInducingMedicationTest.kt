package com.hartwig.actin.algo.evaluation.medication

import com.hartwig.actin.TestPatientFactory
import com.hartwig.actin.algo.datamodel.EvaluationResult
import com.hartwig.actin.algo.evaluation.EvaluationAssert.assertEvaluation
import com.hartwig.actin.clinical.datamodel.CypInteraction
import org.assertj.core.api.Assertions.assertThat
import org.junit.Test

class CurrentlyGetsAnyCypInhibitingOrInducingMedicationTest {
    private val alwaysActiveFunction = CurrentlyGetsAnyCypInhibitingOrInducingMedication(MedicationTestFactory.alwaysActive())
    private val alwaysPlannedFunction = CurrentlyGetsAnyCypInhibitingOrInducingMedication(MedicationTestFactory.alwaysPlanned())

    @Test
    fun `Should pass when any CYP inhibiting or inducing medication`() {
        assertEvaluation(
            EvaluationResult.PASS, alwaysActiveFunction.evaluate(
                MedicationTestFactory.withCypInteraction("9A9", CypInteraction.Type.INDUCER, CypInteraction.Strength.STRONG)
            )
        )
        assertEvaluation(
            EvaluationResult.PASS, alwaysActiveFunction.evaluate(
                MedicationTestFactory.withCypInteraction("9A9", CypInteraction.Type.INHIBITOR, CypInteraction.Strength.STRONG)
            )
        )
    }

    @Test
    fun `Should fail when no CYP inhibiting or inducing medication`() {
        assertEvaluation(
            EvaluationResult.FAIL, alwaysActiveFunction.evaluate(
                MedicationTestFactory.withCypInteraction("9A9", CypInteraction.Type.SUBSTRATE, CypInteraction.Strength.STRONG)
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
                MedicationTestFactory.withCypInteraction("9A9", CypInteraction.Type.INDUCER, CypInteraction.Strength.STRONG)
            )
        )
        assertEvaluation(
            EvaluationResult.WARN, alwaysPlannedFunction.evaluate(
                MedicationTestFactory.withCypInteraction("9A9", CypInteraction.Type.INHIBITOR, CypInteraction.Strength.STRONG)
            )
        )
    }

    @Test
    fun `Should fail when patient plans to use medication which is not CYP inhibiting or inducing`() {
        assertEvaluation(
            EvaluationResult.FAIL, alwaysPlannedFunction.evaluate(
                MedicationTestFactory.withCypInteraction("9A9", CypInteraction.Type.SUBSTRATE, CypInteraction.Strength.STRONG)
            )
        )
    }

    @Test
    fun `Should be undetermined if medication is not provided`() {
        val result = alwaysPlannedFunction.evaluate(
            TestPatientFactory.createMinimalTestPatientRecord().copy(medications = null)
        )
        assertEvaluation(EvaluationResult.UNDETERMINED, result)
        assertThat(result.recoverable).isTrue()
    }
}