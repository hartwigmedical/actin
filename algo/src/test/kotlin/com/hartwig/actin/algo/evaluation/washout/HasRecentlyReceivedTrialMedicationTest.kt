package com.hartwig.actin.algo.evaluation.washout

import com.hartwig.actin.algo.evaluation.EvaluationAssert.assertEvaluation
import com.hartwig.actin.algo.evaluation.medication.MedicationTestFactory
import com.hartwig.actin.algo.evaluation.washout.WashoutTestFactory.medication
import com.hartwig.actin.datamodel.TestPatientFactory
import com.hartwig.actin.datamodel.algo.EvaluationResult
import com.hartwig.actin.datamodel.clinical.Medication
import com.hartwig.actin.datamodel.clinical.TestClinicalFactory
import com.hartwig.actin.datamodel.clinical.TreatmentTestFactory
import com.hartwig.actin.datamodel.clinical.treatment.TreatmentCategory
import org.assertj.core.api.Assertions.assertThat
import org.junit.Test

class HasRecentlyReceivedTrialMedicationTest {

    private val evaluationDate = TestClinicalFactory.createMinimalTestClinicalRecord().patient.registrationDate.plusWeeks(1)
    private val functionActive = HasRecentlyReceivedTrialMedication(MedicationTestFactory.alwaysActive(), evaluationDate.plusDays(1))

    @Test
    fun `Should fail when no medication`() {
        val medications = emptyList<Medication>()
        assertEvaluation(EvaluationResult.FAIL, functionActive.evaluate(MedicationTestFactory.withMedications(medications)))
    }

    @Test
    fun `Should fail when medication is no trial medication`() {
        val medications = listOf(medication(isTrialMedication = false))
        assertEvaluation(EvaluationResult.FAIL, functionActive.evaluate(MedicationTestFactory.withMedications(medications)))
    }

    @Test
    fun `Should pass when medication is trial medication`() {
        val medications = listOf(medication(isTrialMedication = true))
        assertEvaluation(EvaluationResult.PASS, functionActive.evaluate(MedicationTestFactory.withMedications(medications)))
    }

    @Test
    fun `Should pass when medication has correct date`() {
        val function = HasRecentlyReceivedTrialMedication(
            MedicationTestFactory.alwaysStopped(),
            evaluationDate.minusDays(1)
        )
        val medications = listOf(medication(isTrialMedication = true, stopDate = evaluationDate))
        assertEvaluation(EvaluationResult.PASS, function.evaluate(MedicationTestFactory.withMedications(medications)))
    }

    @Test
    fun `Should pass when medication is not trial medication but treatment history entry is trial`() {
        val medications = listOf(medication(isTrialMedication = false))
        val treatments = TreatmentTestFactory.treatment("Chemotherapy", true, setOf(TreatmentCategory.CHEMOTHERAPY))
        val treatmentHistory = listOf(
            TreatmentTestFactory.treatmentHistoryEntry(
                setOf(treatments),
                isTrial = true,
                stopYear = evaluationDate.year,
                stopMonth = evaluationDate.plusMonths(2).monthValue
            )
        )
        assertEvaluation(
            EvaluationResult.PASS,
            functionActive.evaluate(TreatmentTestFactory.withTreatmentsAndMedications(treatmentHistory, medications))
        )
    }

    @Test
    fun `Should evaluate to undetermined when medication stopped after min stop date`() {
        val function = HasRecentlyReceivedTrialMedication(
            MedicationTestFactory.alwaysStopped(),
            evaluationDate.minusWeeks(2)
        )
        val medications = listOf(medication(isTrialMedication = true, stopDate = evaluationDate))
        assertEvaluation(
            EvaluationResult.UNDETERMINED,
            function.evaluate(MedicationTestFactory.withMedications(medications))
        )
    }

    @Test
    fun `Should evaluate to undetermined if medication is not provided and no trial in treatment history entry`() {
        val result = functionActive.evaluate(
            TestPatientFactory.createMinimalTestWGSPatientRecord().copy(medications = null)
        )
        assertEvaluation(EvaluationResult.UNDETERMINED, result)
        assertThat(result.recoverable).isTrue()
    }

    @Test
    fun `Should pass if medication is not provided but trial in treatment history entry`() {
        val treatments = TreatmentTestFactory.treatment("Chemotherapy", true, setOf(TreatmentCategory.CHEMOTHERAPY))
        val treatmentHistory = listOf(
            TreatmentTestFactory.treatmentHistoryEntry(
                setOf(treatments),
                isTrial = true,
                stopYear = evaluationDate.year,
                stopMonth = evaluationDate.plusMonths(2).monthValue
            )
        )
        assertEvaluation(
            EvaluationResult.PASS,
            functionActive.evaluate(TreatmentTestFactory.withTreatmentsAndMedications(treatmentHistory, null))
        )
    }

    @Test
    fun `Should evaluate to undetermined if trial in treatment history entry but with unknown date`() {
        val treatments = TreatmentTestFactory.treatment("Chemotherapy", true, setOf(TreatmentCategory.CHEMOTHERAPY))
        val treatmentHistory = listOf(TreatmentTestFactory.treatmentHistoryEntry(setOf(treatments), isTrial = true))
        assertEvaluation(
            EvaluationResult.UNDETERMINED,
            functionActive.evaluate(TreatmentTestFactory.withTreatmentHistory(treatmentHistory))
        )
    }
}