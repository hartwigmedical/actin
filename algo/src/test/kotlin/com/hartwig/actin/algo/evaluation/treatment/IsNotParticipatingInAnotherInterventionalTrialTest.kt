package com.hartwig.actin.algo.evaluation.treatment

import com.hartwig.actin.algo.evaluation.EvaluationAssert.assertEvaluation
import com.hartwig.actin.algo.evaluation.medication.MedicationTestFactory
import com.hartwig.actin.algo.evaluation.washout.WashoutTestFactory.medication
import com.hartwig.actin.datamodel.TestPatientFactory
import com.hartwig.actin.datamodel.algo.EvaluationResult
import com.hartwig.actin.datamodel.clinical.TreatmentTestFactory
import com.hartwig.actin.datamodel.clinical.treatment.TreatmentCategory
import org.junit.Test
import java.time.LocalDate

class IsNotParticipatingInAnotherInterventionalTrialTest {

    private val referenceDate = LocalDate.of(2025, 2, 2)
    private val alwaysActiveMedicationFunction = IsNotParticipatingInAnotherInterventionalTrial(
        MedicationTestFactory.alwaysActive(),
        referenceDate.minusWeeks(2)
    )

    @Test
    fun `Should warn when patient recently received trial medication`() {
        val medications = listOf(medication(isTrialMedication = true))
        assertEvaluation(
            EvaluationResult.WARN,
            alwaysActiveMedicationFunction.evaluate(MedicationTestFactory.withMedications(medications))
        )
    }

    @Test
    fun `Should warn when patient recently received trial treatment`() {
        val treatments = TreatmentTestFactory.treatment("Chemotherapy", true, setOf(TreatmentCategory.CHEMOTHERAPY))
        val treatmentHistory = listOf(
            TreatmentTestFactory.treatmentHistoryEntry(
                setOf(treatments),
                isTrial = true,
                startYear = referenceDate.year,
                startMonth = referenceDate.monthValue
            )
        )
        assertEvaluation(
            EvaluationResult.WARN,
            alwaysActiveMedicationFunction.evaluate(TreatmentTestFactory.withTreatmentsAndMedications(treatmentHistory, null))
        )
    }

    @Test
    fun `Should return not evaluated when patient received non recent trial medication`() {
        val alwaysStoppedMedicationFunction =
            IsNotParticipatingInAnotherInterventionalTrial(MedicationTestFactory.alwaysStopped(), referenceDate)
        val medications = listOf(medication(isTrialMedication = true))
        assertEvaluation(
            EvaluationResult.NOT_EVALUATED,
            alwaysStoppedMedicationFunction.evaluate(MedicationTestFactory.withMedications(medications))
        )
    }

    @Test
    fun `Should return not evaluated when patient received non recent trial treatment`() {
        val treatments = TreatmentTestFactory.treatment("Chemotherapy", true, setOf(TreatmentCategory.CHEMOTHERAPY))
        val treatmentHistory = listOf(
            TreatmentTestFactory.treatmentHistoryEntry(
                setOf(treatments),
                isTrial = true,
                stopYear = referenceDate.year - 1,
            )
        )
        assertEvaluation(
            EvaluationResult.NOT_EVALUATED,
            alwaysActiveMedicationFunction.evaluate(TreatmentTestFactory.withTreatmentsAndMedications(treatmentHistory, null))
        )
    }

    @Test
    fun `Should return not evaluated when patient received no trial treatment or medication`() {
        assertEvaluation(
            EvaluationResult.NOT_EVALUATED,
            alwaysActiveMedicationFunction.evaluate(TestPatientFactory.createMinimalTestWGSPatientRecord())
        )
    }
}