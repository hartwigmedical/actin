package com.hartwig.actin.algo.evaluation.medication

import com.hartwig.actin.algo.evaluation.EvaluationAssert.assertEvaluation
import com.hartwig.actin.datamodel.TestPatientFactory
import com.hartwig.actin.datamodel.algo.EvaluationResult
import com.hartwig.actin.datamodel.clinical.DrugInteraction
import org.assertj.core.api.Assertions.assertThat
import org.junit.jupiter.api.Test

class CurrentlyGetsAnyCypMedicationOfTypesTest {
    private val alwaysActiveFunction = CurrentlyGetsAnyCypMedicationOfTypes(
        MedicationTestFactory.alwaysActive(),
        setOf(DrugInteraction.Type.INDUCER, DrugInteraction.Type.INHIBITOR)
    )
    private val alwaysPlannedFunction = CurrentlyGetsAnyCypMedicationOfTypes(
        MedicationTestFactory.alwaysPlanned(),
        setOf(DrugInteraction.Type.INDUCER, DrugInteraction.Type.INHIBITOR)
    )

    @Test
    fun `Should pass when any CYP inhibiting or inducing medication`() {
        assertEvaluation(
            EvaluationResult.PASS, alwaysActiveFunction.evaluate(
                MedicationTestFactory.withCypInteraction("9A9", DrugInteraction.Type.INDUCER, DrugInteraction.Strength.STRONG, "name")
            ),
            "CYP inducer or inhibitor medication (name) in provided medications"
        )
        assertEvaluation(
            EvaluationResult.PASS, alwaysActiveFunction.evaluate(
                MedicationTestFactory.withCypInteraction("9A9", DrugInteraction.Type.INHIBITOR, DrugInteraction.Strength.STRONG, "name")
            ),
            "CYP inducer or inhibitor medication (name) in provided medications"
        )
    }

    @Test
    fun `Should fail when no CYP inhibiting or inducing medication`() {
        assertEvaluation(
            EvaluationResult.FAIL, alwaysActiveFunction.evaluate(
                MedicationTestFactory.withCypInteraction("9A9", DrugInteraction.Type.SUBSTRATE, DrugInteraction.Strength.STRONG)
            ),
            "No CYP inducer or inhibitor medication in provided medications"
        )
    }

    @Test
    fun `Should fail when patient uses no medication`() {
        assertEvaluation(
            EvaluationResult.FAIL,
            alwaysActiveFunction.evaluate(MedicationTestFactory.withMedications(emptyList())),
            "No CYP inducer or inhibitor medication in provided medications"
        )
    }

    @Test
    fun `Should warn when patient plans to use CYP inhibiting or inducing medication`() {
        assertEvaluation(
            EvaluationResult.WARN, alwaysPlannedFunction.evaluate(
                MedicationTestFactory.withCypInteraction("9A9", DrugInteraction.Type.INDUCER, DrugInteraction.Strength.STRONG, "name")
            ),
            "Planned CYP inducer or inhibitor medication (name)"
        )
        assertEvaluation(
            EvaluationResult.WARN, alwaysPlannedFunction.evaluate(
                MedicationTestFactory.withCypInteraction("9A9", DrugInteraction.Type.INHIBITOR, DrugInteraction.Strength.STRONG, "name")
            ),
            "Planned CYP inducer or inhibitor medication (name)"
        )
    }

    @Test
    fun `Should fail when patient plans to use medication which is not CYP inhibiting or inducing`() {
        assertEvaluation(
            EvaluationResult.FAIL, alwaysPlannedFunction.evaluate(
                MedicationTestFactory.withCypInteraction("9A9", DrugInteraction.Type.SUBSTRATE, DrugInteraction.Strength.STRONG)
            ),
            "No CYP inducer or inhibitor medication in provided medications"
        )
    }

    @Test
    fun `Should be undetermined if medication is not provided`() {
        val result = alwaysPlannedFunction.evaluate(
            TestPatientFactory.createMinimalTestWGSPatientRecord().copy(medications = null)
        )
        assertEvaluation(EvaluationResult.UNDETERMINED, result, "No medication data provided")
        assertThat(result.recoverable).isTrue()
    }
}