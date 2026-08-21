package com.hartwig.actin.algo.evaluation.medication

import com.hartwig.actin.algo.evaluation.EvaluationAssert.assertEvaluation
import com.hartwig.actin.datamodel.TestPatientFactory
import com.hartwig.actin.datamodel.algo.EvaluationResult
import com.hartwig.actin.datamodel.clinical.DrugInteraction
import org.assertj.core.api.Assertions.assertThat
import org.junit.jupiter.api.Test

class CurrentlyGetsTransporterInteractingMedicationTest {
    private val patientWithBCRPSubstrateMedication =
        MedicationTestFactory.withTransporterInteraction("BCRP", DrugInteraction.Type.SUBSTRATE, DrugInteraction.Strength.UNKNOWN)
    private val patientWithBCRPInhibitorMedication =
        MedicationTestFactory.withTransporterInteraction("BCRP", DrugInteraction.Type.INHIBITOR, DrugInteraction.Strength.UNKNOWN)

    @Test
    fun `Should pass with active expected BCRP medication`() {
        assertEvaluation(
            EvaluationResult.PASS,
            createFunction(
                MedicationTestFactory.alwaysActive(),
                DrugInteraction.Type.SUBSTRATE
            ).evaluate(patientWithBCRPSubstrateMedication),
            "Active BCRP substrate medication use ()"
        )
        assertEvaluation(
            EvaluationResult.PASS,
            createFunction(
                MedicationTestFactory.alwaysActive(),
                DrugInteraction.Type.INHIBITOR
            ).evaluate(patientWithBCRPInhibitorMedication),
            "Active BCRP inhibitor medication use ()"
        )
    }

    @Test
    fun `Should warn with planned expected BCRP medication`() {
        assertEvaluation(
            EvaluationResult.WARN,
            createFunction(MedicationTestFactory.alwaysPlanned(), DrugInteraction.Type.SUBSTRATE).evaluate(
                patientWithBCRPSubstrateMedication
            ),
            "Planned BCRP substrate medication use ()"
        )

        assertEvaluation(
            EvaluationResult.WARN,
            createFunction(MedicationTestFactory.alwaysPlanned(), DrugInteraction.Type.INHIBITOR).evaluate(
                patientWithBCRPInhibitorMedication
            ),
            "Planned BCRP inhibitor medication use ()"
        )
    }

    @Test
    fun `Should fail without BCRP expected medication`() {
        assertEvaluation(
            EvaluationResult.FAIL,
            createFunction(
                MedicationTestFactory.alwaysActive(),
                DrugInteraction.Type.SUBSTRATE
            ).evaluate(patientWithBCRPInhibitorMedication),
            "No current BCRP substrate medication use"
        )

        assertEvaluation(
            EvaluationResult.FAIL,
            createFunction(MedicationTestFactory.alwaysPlanned(), DrugInteraction.Type.INHIBITOR).evaluate(
                patientWithBCRPSubstrateMedication
            ),
            "No current BCRP inhibitor medication use"
        )
    }

    @Test
    fun `Should fail when patient uses no medication`() {
        assertEvaluation(
            EvaluationResult.FAIL,
            createFunction(
                MedicationTestFactory.alwaysActive(),
                DrugInteraction.Type.SUBSTRATE
            ).evaluate(MedicationTestFactory.withMedications(emptyList())),
            "No current BCRP substrate medication use"
        )
    }

    @Test
    fun `Should be undetermined if medication is not provided`() {
        val result = createFunction(
            MedicationTestFactory.alwaysActive(),
            DrugInteraction.Type.SUBSTRATE
        ).evaluate(TestPatientFactory.createMinimalTestWGSPatientRecord().copy(medications = null))

        assertEvaluation(EvaluationResult.UNDETERMINED, result, "No medication data provided")
        assertThat(result.recoverable).isTrue()
    }

    private fun createFunction(selector: MedicationSelector, type: DrugInteraction.Type): CurrentlyGetsTransporterInteractingMedication {
        return CurrentlyGetsTransporterInteractingMedication(selector, "BCRP", type)
    }
}