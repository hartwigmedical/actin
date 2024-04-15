package com.hartwig.actin.algo.evaluation.medication

import com.hartwig.actin.TestPatientFactory
import com.hartwig.actin.algo.datamodel.EvaluationResult
import com.hartwig.actin.algo.evaluation.EvaluationAssert
import org.assertj.core.api.Assertions.assertThat
import org.junit.Test

class CurrentlyGetsHerbalMedicationTest {
    private val alwaysActiveFunction = CurrentlyGetsHerbalMedication(MedicationTestFactory.alwaysActive())
    private val alwaysPlannedFunction = CurrentlyGetsHerbalMedication(MedicationTestFactory.alwaysPlanned())
    private val alwaysInactiveFunction = CurrentlyGetsHerbalMedication(MedicationTestFactory.alwaysInactive())

    @Test
    fun `Should fail when patient uses no medications`() {
        EvaluationAssert.assertEvaluation(
            EvaluationResult.FAIL,
            alwaysActiveFunction.evaluate(MedicationTestFactory.withMedications(emptyList()))
        )
    }

    @Test
    fun `Should be fail when no self care medication`() {
        val medications = listOf(MedicationTestFactory.medication())
        EvaluationAssert.assertEvaluation(
            EvaluationResult.FAIL,
            alwaysActiveFunction.evaluate(
                MedicationTestFactory.withMedications(medications)
            )
        )
    }

    @Test
    fun `Should be undetermined when medication is self care and active or planned`() {
        val medications = listOf(MedicationTestFactory.medication(isSelfCare = true))
        EvaluationAssert.assertEvaluation(
            EvaluationResult.UNDETERMINED,
            alwaysActiveFunction.evaluate(
                MedicationTestFactory.withMedications(medications)
            )
        )
        EvaluationAssert.assertEvaluation(
            EvaluationResult.UNDETERMINED,
            alwaysPlannedFunction.evaluate(
                MedicationTestFactory.withMedications(medications)
            )
        )
    }

    @Test
    fun `Should be fail when medication is self care but not active or planned`() {
        val medications = listOf(MedicationTestFactory.medication(isSelfCare = true))
        EvaluationAssert.assertEvaluation(
            EvaluationResult.FAIL,
            alwaysInactiveFunction.evaluate(
                MedicationTestFactory.withMedications(medications)
            )
        )
    }

    @Test
    fun `Should be undetermined if medication is not provided`() {
        val medicationNotProvided = TestPatientFactory.createMinimalTestWGSPatientRecord().copy(medications = null)
        val alwaysPlannedResult = alwaysPlannedFunction.evaluate(medicationNotProvided)
        EvaluationAssert.assertEvaluation(EvaluationResult.UNDETERMINED, alwaysPlannedResult)
        assertThat(alwaysPlannedResult.recoverable).isTrue()
        val alwaysActiveResult = alwaysActiveFunction.evaluate(medicationNotProvided)
        EvaluationAssert.assertEvaluation(EvaluationResult.UNDETERMINED, alwaysActiveResult)
        assertThat(alwaysActiveResult.recoverable).isTrue()
    }
}