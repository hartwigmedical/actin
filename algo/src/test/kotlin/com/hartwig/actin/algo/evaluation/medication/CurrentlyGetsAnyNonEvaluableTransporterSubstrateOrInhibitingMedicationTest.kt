package com.hartwig.actin.algo.evaluation.medication

import com.hartwig.actin.algo.evaluation.EvaluationAssert.assertEvaluation
import com.hartwig.actin.datamodel.TestPatientFactory
import com.hartwig.actin.datamodel.algo.EvaluationResult
import org.assertj.core.api.Assertions.assertThat
import org.junit.Test

class CurrentlyGetsAnyNonEvaluableTransporterSubstrateOrInhibitingMedicationTest {
    private val patientWithMedication = MedicationTestFactory.withMedications(listOf(MedicationTestFactory.medication("Some medication")))

    @Test
    fun `Should fail when patient medication list is empty`() {
        assertEvaluation(EvaluationResult.FAIL, createFunction(MedicationTestFactory.alwaysActive()).evaluate(MedicationTestFactory.withMedications(emptyList())))
    }

    @Test
    fun `Should fail when no active or planned medication`() {
        assertEvaluation(EvaluationResult.FAIL, createFunction(MedicationTestFactory.alwaysInactive()).evaluate(patientWithMedication))
    }

    @Test
    fun `Should warn when any planned or active medication`() {
        val resultPlanned = createFunction(MedicationTestFactory.alwaysPlanned()).evaluate(patientWithMedication)
        val resultActive = createFunction(MedicationTestFactory.alwaysActive()).evaluate(patientWithMedication)

        assertEvaluation(EvaluationResult.WARN, resultPlanned)
        assertEvaluation(EvaluationResult.WARN, resultActive)
        assertThat(resultActive.warnGeneralMessages).containsExactly("Undetermined if patient may use TYPE-A, type-B or TYPE-C substrate or inhibiting medication")
    }

    @Test
    fun `Should be undetermined if medication is not provided`() {
        val result = createFunction(MedicationTestFactory.alwaysActive()).evaluate(TestPatientFactory.createMinimalTestWGSPatientRecord().copy(medications = null))

        assertEvaluation(EvaluationResult.UNDETERMINED, result)
        assertThat(result.recoverable).isTrue()
    }

    private fun createFunction(selector: MedicationSelector): CurrentlyGetsAnyNonEvaluableTransporterSubstrateOrInhibitingMedication {
        val types = listOf("TYPE-A", "type-B", "TYPE-C")
        return CurrentlyGetsAnyNonEvaluableTransporterSubstrateOrInhibitingMedication(selector, types)
    }
}