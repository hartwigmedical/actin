package com.hartwig.actin.algo.evaluation.medication

import com.hartwig.actin.algo.datamodel.EvaluationResult
import com.hartwig.actin.algo.evaluation.EvaluationAssert.assertEvaluation
import com.hartwig.actin.clinical.datamodel.CypInteraction
import com.hartwig.actin.clinical.datamodel.TestClinicalFactory
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
                TARGET_CYP, CypInteraction.Type.INDUCER, CypInteraction.Strength.STRONG, evaluationDate
            )
        )
        assertEvaluation(EvaluationResult.PASS, function.evaluate(MedicationTestFactory.withMedications(medications)))
    }

    @Test
    fun `Should fail when patient recently received CYP-inducing medication that does not match CYP`() {
        val medications = listOf(
            MedicationTestFactory.medicationWithCypInteraction(
                "3A4", CypInteraction.Type.INDUCER, CypInteraction.Strength.STRONG, evaluationDate
            )
        )
        assertEvaluation(EvaluationResult.FAIL, function.evaluate(MedicationTestFactory.withMedications(medications)))
    }

    @Test
    fun `Should fail when patient received CYP-inducing medication before min stop date`() {
        val medications = listOf(
            MedicationTestFactory.medicationWithCypInteraction(
                TARGET_CYP, CypInteraction.Type.INDUCER, CypInteraction.Strength.STRONG, evaluationDate.minusWeeks(3)
            )
        )
        assertEvaluation(EvaluationResult.FAIL, function.evaluate(MedicationTestFactory.withMedications(medications)))
    }

    @Test
    fun `Should fail when patient does not recently received CYP-inducing medication`() {
        val medications = listOf(
            MedicationTestFactory.medicationWithCypInteraction(
                TARGET_CYP, CypInteraction.Type.SUBSTRATE, CypInteraction.Strength.STRONG, evaluationDate
            )
        )
        assertEvaluation(EvaluationResult.FAIL, function.evaluate(MedicationTestFactory.withMedications(medications)))
    }

    @Test
    fun `Should fail when patient uses no medication`() {
        assertEvaluation(EvaluationResult.FAIL, function.evaluate(MedicationTestFactory.withMedications(emptyList())))
    }
}