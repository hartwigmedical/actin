package com.hartwig.actin.algo.evaluation.medication

import com.hartwig.actin.algo.evaluation.EvaluationAssert.assertEvaluation
import com.hartwig.actin.datamodel.TestPatientFactory
import com.hartwig.actin.datamodel.algo.EvaluationResult
import com.hartwig.actin.datamodel.clinical.DrugInteraction
import com.hartwig.actin.datamodel.clinical.TestClinicalFactory
import org.assertj.core.api.Assertions.assertThat
import org.junit.Test

private const val TARGET_CYP = "9A9"

class HasRecentlyReceivedCypXInducingMedicationTest {
    private val evaluationDate = TestClinicalFactory.createMinimalTestClinicalRecord().patient.registrationDate.plusWeeks(1)
    private val function =
        HasRecentlyReceivedCypXInducingMedication(MedicationTestFactory.alwaysStopped(), TARGET_CYP, evaluationDate.minusDays(1))
    
    @Test
    fun `Should pass when patient recently received CYP-inducing medication`() {
        val medications = listOf(
            MedicationTestFactory.medicationWithCypInteraction(
                TARGET_CYP, DrugInteraction.Type.INDUCER, DrugInteraction.Strength.STRONG, evaluationDate
            )
        )
        assertEvaluation(EvaluationResult.PASS, function.evaluate(MedicationTestFactory.withMedications(medications)))
    }

    @Test
    fun `Should fail when patient recently received CYP-inducing medication that does not match CYP`() {
        val medications = listOf(
            MedicationTestFactory.medicationWithCypInteraction(
                "3A4", DrugInteraction.Type.INDUCER, DrugInteraction.Strength.STRONG, evaluationDate
            )
        )
        assertEvaluation(EvaluationResult.FAIL, function.evaluate(MedicationTestFactory.withMedications(medications)))
    }

    @Test
    fun `Should fail when patient received CYP-inducing medication before min stop date`() {
        val medications = listOf(
            MedicationTestFactory.medicationWithCypInteraction(
                TARGET_CYP, DrugInteraction.Type.INDUCER, DrugInteraction.Strength.STRONG, evaluationDate.minusWeeks(3)
            )
        )
        assertEvaluation(EvaluationResult.FAIL, function.evaluate(MedicationTestFactory.withMedications(medications)))
    }

    @Test
    fun `Should fail when patient does not recently received CYP-inducing medication`() {
        val medications = listOf(
            MedicationTestFactory.medicationWithCypInteraction(
                TARGET_CYP, DrugInteraction.Type.SUBSTRATE, DrugInteraction.Strength.STRONG, evaluationDate
            )
        )
        assertEvaluation(EvaluationResult.FAIL, function.evaluate(MedicationTestFactory.withMedications(medications)))
    }

    @Test
    fun `Should fail when patient uses no medication`() {
        assertEvaluation(EvaluationResult.FAIL, function.evaluate(MedicationTestFactory.withMedications(emptyList())))
    }

    @Test
    fun `Should be undetermined if medication is not provided`() {
        val result = function.evaluate(
            TestPatientFactory.createMinimalTestWGSPatientRecord().copy(medications = null)
        )
        assertEvaluation(EvaluationResult.UNDETERMINED, result)
        assertThat(result.recoverable).isTrue()
    }
}