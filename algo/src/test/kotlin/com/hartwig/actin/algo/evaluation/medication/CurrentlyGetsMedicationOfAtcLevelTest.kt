package com.hartwig.actin.algo.evaluation.medication

import com.hartwig.actin.algo.evaluation.EvaluationAssert.assertEvaluation
import com.hartwig.actin.datamodel.TestPatientFactory
import com.hartwig.actin.datamodel.algo.EvaluationResult
import com.hartwig.actin.datamodel.clinical.AtcLevel
import com.hartwig.actin.datamodel.clinical.TestMedicationFactory
import org.assertj.core.api.Assertions.assertThat
import org.junit.Test

private const val TARGET_ATC_CODE = "L01A"

class CurrentlyGetsMedicationOfAtcLevelTest {
    private val targetAtcLevel = AtcLevel(code = TARGET_ATC_CODE, name = "")
    private val alwaysActiveFunction =
        CurrentlyGetsMedicationOfAtcLevel(MedicationTestFactory.alwaysActive(), TARGET_ATC_CODE, setOf(targetAtcLevel))

    private val alwaysPlannedFunction =
        CurrentlyGetsMedicationOfAtcLevel(MedicationTestFactory.alwaysPlanned(), TARGET_ATC_CODE, setOf(targetAtcLevel))

    @Test
    fun `Should fail when no medication`() {
        assertEvaluation(EvaluationResult.FAIL, alwaysActiveFunction.evaluate(MedicationTestFactory.withMedications(emptyList())))
    }

    @Test
    fun `Should fail when medication has wrong category`() {
        assertEvaluation(EvaluationResult.FAIL, alwaysActiveFunction.evaluate(patientWithMedicationHavingAnatomicalCode("wrong category")))
    }

    @Test
    fun `Should pass when medication has right category`() {
        assertEvaluation(
            EvaluationResult.PASS, alwaysActiveFunction.evaluate(patientWithMedicationHavingAnatomicalCode(TARGET_ATC_CODE))
        )
    }

    @Test
    fun `Should warn when patient plans to use medication of right category`() {
        assertEvaluation(
            EvaluationResult.WARN, alwaysPlannedFunction.evaluate(patientWithMedicationHavingAnatomicalCode(TARGET_ATC_CODE))
        )
    }

    @Test
    fun `Should fail when patient plans to use medication with wrong category`() {
        assertEvaluation(EvaluationResult.FAIL, alwaysPlannedFunction.evaluate(patientWithMedicationHavingAnatomicalCode("wrong category")))
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

    private fun patientWithMedicationHavingAnatomicalCode(atcCode: String) = MedicationTestFactory.withMedications(
        listOf(TestMedicationFactory.createMinimal().copy(atc = AtcTestFactory.atcClassification(atcCode)))
    )
}