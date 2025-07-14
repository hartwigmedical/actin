package com.hartwig.actin.algo.evaluation.treatment

import com.hartwig.actin.algo.evaluation.EvaluationAssert.assertEvaluation
import com.hartwig.actin.algo.evaluation.medication.MedicationTestFactory
import com.hartwig.actin.algo.evaluation.washout.WashoutTestFactory.medication
import com.hartwig.actin.datamodel.TestPatientFactory
import com.hartwig.actin.datamodel.algo.EvaluationResult
import org.junit.Test
import java.time.LocalDate

class IsNotParticipatingInAnotherInterventionalTrialTest {

    private val minStopDate = LocalDate.of(2025, 2, 2)
    private val function = IsNotParticipatingInAnotherInterventionalTrial(MedicationTestFactory.alwaysActive(), minStopDate)

    @Test
    fun `Should warn when patient recently received trial medication`() {
        val medications = listOf(medication(isTrialMedication = true))
        assertEvaluation(
            EvaluationResult.WARN,
            function.evaluate(MedicationTestFactory.withMedications(medications))
        )
    }

    @Test
    fun `Should return not evaluated when patient had non recent trial medication`() {
        val stoppedFunction = IsNotParticipatingInAnotherInterventionalTrial(MedicationTestFactory.alwaysStopped(), minStopDate)
        val medications = listOf(medication(isTrialMedication = true))
        assertEvaluation(
            EvaluationResult.NOT_EVALUATED,
            stoppedFunction.evaluate(MedicationTestFactory.withMedications(medications))
        )
    }

    @Test
    fun `Should return not evaluated when patient had no trial medication`() {
        assertEvaluation(
            EvaluationResult.NOT_EVALUATED,
            function.evaluate(TestPatientFactory.createMinimalTestWGSPatientRecord())
        )
    }
}