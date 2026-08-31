package com.hartwig.actin.algo.evaluation.medication

import com.hartwig.actin.algo.evaluation.EvaluationAssert
import com.hartwig.actin.datamodel.TestPatientFactory
import com.hartwig.actin.datamodel.algo.EvaluationResult
import org.assertj.core.api.Assertions.assertThat
import org.junit.jupiter.api.Test

class CurrentlyGetsHerbalMedicationTest {
    private val alwaysActiveFunction = CurrentlyGetsHerbalMedication(MedicationTestFactory.alwaysActive())
    private val alwaysPlannedFunction = CurrentlyGetsHerbalMedication(MedicationTestFactory.alwaysPlanned())
    private val alwaysInactiveFunction = CurrentlyGetsHerbalMedication(MedicationTestFactory.alwaysInactive())

    @Test
    fun `Should fail when patient uses no medications`() {
        EvaluationAssert.assertEvaluation(
            EvaluationResult.FAIL,
            alwaysActiveFunction.evaluate(MedicationTestFactory.withMedications(emptyList())),
            "No use of herbal medications"
        )
    }

    @Test
    fun `Should fail when no self care medication`() {
        val medications = listOf(MedicationTestFactory.medication())
        EvaluationAssert.assertEvaluation(
            EvaluationResult.FAIL,
            alwaysActiveFunction.evaluate(
                MedicationTestFactory.withMedications(medications)
            ),
            "No use of herbal medications"
        )
    }

    @Test
    fun `Should be undetermined when medication is self care and active or planned`() {
        val medications = listOf(MedicationTestFactory.medication(isSelfCare = true))
        EvaluationAssert.assertEvaluation(
            EvaluationResult.UNDETERMINED,
            alwaysActiveFunction.evaluate(
                MedicationTestFactory.withMedications(medications)
            ),
            "Undetermined if herbal medications may be used (self care medication use)"
        )
        EvaluationAssert.assertEvaluation(
            EvaluationResult.UNDETERMINED,
            alwaysPlannedFunction.evaluate(
                MedicationTestFactory.withMedications(medications)
            ),
            "Undetermined if use of herbal medications may be planned (planned self care medication use)"
        )
    }

    @Test
    fun `Should fail when medication is self care but not active or planned`() {
        val medications = listOf(MedicationTestFactory.medication(isSelfCare = true))
        EvaluationAssert.assertEvaluation(
            EvaluationResult.FAIL,
            alwaysInactiveFunction.evaluate(
                MedicationTestFactory.withMedications(medications)
            ),
            "No use of herbal medications"
        )
    }

    @Test
    fun `Should be undetermined if medication is not provided`() {
        val medicationNotProvided = TestPatientFactory.createMinimalTestWGSPatientRecord().copy(medications = null)
        val alwaysPlannedResult = alwaysPlannedFunction.evaluate(medicationNotProvided)
        EvaluationAssert.assertEvaluation(EvaluationResult.UNDETERMINED, alwaysPlannedResult, "No medication data provided")
        assertThat(alwaysPlannedResult.recoverable).isTrue()
        val alwaysActiveResult = alwaysActiveFunction.evaluate(medicationNotProvided)
        EvaluationAssert.assertEvaluation(EvaluationResult.UNDETERMINED, alwaysActiveResult, "No medication data provided")
        assertThat(alwaysActiveResult.recoverable).isTrue()
    }
}