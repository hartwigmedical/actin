package com.hartwig.actin.algo.evaluation.medication

import com.hartwig.actin.algo.evaluation.EvaluationAssert.assertEvaluation
import com.hartwig.actin.datamodel.TestPatientFactory
import com.hartwig.actin.datamodel.algo.EvaluationResult
import org.assertj.core.api.Assertions.assertThat
import org.junit.Test

class CurrentlyGetsAnyOtherSubstrateOrInhibitingMedicationTest {
    private val types = listOf("TYPE-A", "type-B", "TYPE-C")
    private val alwaysActiveFunction = CurrentlyGetsAnyOtherSubstrateOrInhibitingMedication(MedicationTestFactory.alwaysActive(), types)
    private val alwaysPlannedFunction = CurrentlyGetsAnyOtherSubstrateOrInhibitingMedication(MedicationTestFactory.alwaysPlanned(), types)
    private val alwaysInactiveFunction = CurrentlyGetsAnyOtherSubstrateOrInhibitingMedication(MedicationTestFactory.alwaysInactive(), types)

    @Test
    fun `Should be fail when patient medication list is empty`() {
        assertEvaluation(EvaluationResult.FAIL, alwaysActiveFunction.evaluate(MedicationTestFactory.withMedications(emptyList())))
    }

    @Test
    fun `Should be fail when no active or planned medication`() {
        assertEvaluation(
            EvaluationResult.FAIL,
            alwaysInactiveFunction.evaluate(MedicationTestFactory.withMedications(listOf(MedicationTestFactory.medication("Any medication"))))
        )
    }

    @Test
    fun `Should be warn when any planned or active medication`() {
        val resultPlanned =
            alwaysPlannedFunction.evaluate(MedicationTestFactory.withMedications(listOf(MedicationTestFactory.medication("Any medication"))))
        val resultActive =
            alwaysActiveFunction.evaluate(MedicationTestFactory.withMedications(listOf(MedicationTestFactory.medication("Any medication"))))

        assertEvaluation(EvaluationResult.WARN, resultPlanned)
        assertEvaluation(EvaluationResult.WARN, resultActive)
        assertThat(resultActive.warnGeneralMessages).containsExactly("Undetermined if patient may use TYPE-A, type-B or TYPE-C substrate or inhibiting medication")
    }

    @Test
    fun `Should be undetermined if medication is not provided`() {
        val result = alwaysActiveFunction.evaluate(TestPatientFactory.createMinimalTestWGSPatientRecord().copy(medications = null))

        assertEvaluation(EvaluationResult.UNDETERMINED, result)
        assertThat(result.recoverable).isTrue()
    }
}