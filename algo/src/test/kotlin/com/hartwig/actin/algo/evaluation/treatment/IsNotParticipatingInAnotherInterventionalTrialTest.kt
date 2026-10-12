package com.hartwig.actin.algo.evaluation.treatment

import com.hartwig.actin.algo.evaluation.EvaluationAssert.assertEvaluation
import com.hartwig.actin.algo.evaluation.medication.MedicationTestFactory
import com.hartwig.actin.algo.evaluation.washout.WashoutTestFactory.medication
import com.hartwig.actin.datamodel.TestPatientFactory
import com.hartwig.actin.datamodel.algo.EvaluationResult
import com.hartwig.actin.datamodel.clinical.TreatmentTestFactory
import com.hartwig.actin.datamodel.clinical.treatment.TreatmentCategory
import java.time.LocalDate
import org.junit.jupiter.api.Test

class IsNotParticipatingInAnotherInterventionalTrialTest {

    private val referenceDate = LocalDate.of(2025, 2, 2)
    private val recentDate = referenceDate.plusMonths(1)
    private val olderDate = referenceDate.minusMonths(1)
    private val alwaysActiveMedicationFunction = IsNotParticipatingInAnotherInterventionalTrial(
        MedicationTestFactory.alwaysActive(),
        referenceDate.minusWeeks(2)
    )

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
            alwaysActiveMedicationFunction.evaluate(
                TreatmentTestFactory.withTreatmentsAndMedications(
                    treatmentHistory,
                    null
                )
            ),
            "Recent trial treatment - undetermined participation in another interventional trial"
        )
    }

    @Test
    fun `Should warn when patient recently received trial medication`() {
        val medications = listOf(medication(isTrialMedication = true))
        assertEvaluation(
            EvaluationResult.WARN,
            alwaysActiveMedicationFunction.evaluate(MedicationTestFactory.withMedications(medications)),
            "Recent trial treatment - undetermined participation in another interventional trial"
        )
    }

    @Test
    fun `Should be undetermined when patient received trial treatment without dates`() {
        val treatments = TreatmentTestFactory.treatment("Chemotherapy", true, setOf(TreatmentCategory.CHEMOTHERAPY))
        val treatmentHistory = listOf(
            TreatmentTestFactory.treatmentHistoryEntry(
                setOf(treatments),
                isTrial = true,
                startYear = null,
                startMonth = null
            )
        )
        assertEvaluation(
            EvaluationResult.UNDETERMINED,
            alwaysActiveMedicationFunction.evaluate(
                TreatmentTestFactory.withTreatmentsAndMedications(
                    treatmentHistory,
                    null
                )
            ),
            "Undetermined participation in another interventional trial (missing stop date)"
        )
    }

    @Test
    fun `Should be undetermined when patient received trial treatment potentially after min date`() {
        val treatments = TreatmentTestFactory.treatment("Chemotherapy", true, setOf(TreatmentCategory.CHEMOTHERAPY))
        val treatmentHistory = listOf(
            TreatmentTestFactory.treatmentHistoryEntry(
                setOf(treatments),
                isTrial = true,
                startYear = olderDate.year,
                startMonth = olderDate.monthValue,
                maxStopYear = recentDate.year,
                maxStopMonth = recentDate.monthValue
            )
        )
        assertEvaluation(
            EvaluationResult.UNDETERMINED,
            alwaysActiveMedicationFunction.evaluate(TreatmentTestFactory.withTreatmentsAndMedications(treatmentHistory, null)),
            "Undetermined participation in another interventional trial (missing stop date)"
        )
    }

    @Test
    fun `Should pass when patient received non recent trial medication`() {
        val alwaysStoppedMedicationFunction =
            IsNotParticipatingInAnotherInterventionalTrial(MedicationTestFactory.alwaysStopped(), referenceDate)
        val medications = listOf(medication(isTrialMedication = true))
        assertEvaluation(
            EvaluationResult.PASS,
            alwaysStoppedMedicationFunction.evaluate(MedicationTestFactory.withMedications(medications)),
            "Assumed no participation in another interventional trial"
        )
    }

    @Test
    fun `Should pass when patient received non recent trial treatment`() {
        val treatments = TreatmentTestFactory.treatment("Chemotherapy", true, setOf(TreatmentCategory.CHEMOTHERAPY))
        val treatmentHistory = listOf(
            TreatmentTestFactory.treatmentHistoryEntry(
                setOf(treatments),
                isTrial = true,
                stopYear = referenceDate.year - 1,
            )
        )
        assertEvaluation(
            EvaluationResult.PASS,
            alwaysActiveMedicationFunction.evaluate(TreatmentTestFactory.withTreatmentsAndMedications(treatmentHistory, null)),
            "Assumed no participation in another interventional trial"
        )
    }

    @Test
    fun `Should pass when patient received no trial treatment or medication`() {
        assertEvaluation(
            EvaluationResult.PASS,
            alwaysActiveMedicationFunction.evaluate(TestPatientFactory.createMinimalTestWGSPatientRecord()),
            "Assumed no participation in another interventional trial"
        )
    }
}