package com.hartwig.actin.algo.medication

import com.hartwig.actin.clinical.datamodel.Medication
import com.hartwig.actin.clinical.datamodel.MedicationStatus
import com.hartwig.actin.clinical.datamodel.TestMedicationFactory
import org.junit.Assert.assertEquals
import org.junit.Test
import java.time.LocalDate

class MedicationStatusInterpreterOnEvaluationDateTest {
    @Test
    fun canInterpretMedicationStatus() {
        val evaluationDate = LocalDate.of(2020, 5, 6)
        val interpreter = MedicationStatusInterpreterOnEvaluationDate(evaluationDate)
        val noStatusOrStartOrStop = create(null, null, null)
        assertEquals(MedicationStatusInterpretation.UNKNOWN, interpreter.interpret(noStatusOrStartOrStop))
        val noStatusStartedAndNotStopped = create (null, evaluationDate.minusDays(1),  evaluationDate.plusDays(1))
        assertEquals(MedicationStatusInterpretation.ACTIVE, interpreter.interpret(noStatusStartedAndNotStopped))
        val cancelled = create(MedicationStatus.CANCELLED, null, null)
        assertEquals(MedicationStatusInterpretation.CANCELLED, interpreter.interpret(cancelled))
        val startedButCancelled = create(MedicationStatus.CANCELLED, evaluationDate.minusDays(1), evaluationDate.plusDays(1))
        assertEquals(MedicationStatusInterpretation.CANCELLED, interpreter.interpret(startedButCancelled))
        val activeWithoutStart = create(MedicationStatus.ACTIVE, null, null)
        assertEquals(MedicationStatusInterpretation.UNKNOWN, interpreter.interpret(activeWithoutStart))
        val activeStartingInFuture = create(MedicationStatus.ACTIVE, evaluationDate.plusDays(1), null)
        assertEquals(MedicationStatusInterpretation.PLANNED, interpreter.interpret(activeStartingInFuture))
        val activeAndStarted = create(MedicationStatus.ACTIVE, evaluationDate.minusDays(1), null)
        assertEquals(MedicationStatusInterpretation.ACTIVE, interpreter.interpret(activeAndStarted))
        val startedButOnHold = create(MedicationStatus.ON_HOLD, evaluationDate.minusDays(1), null)
        assertEquals(MedicationStatusInterpretation.STOPPED, interpreter.interpret(startedButOnHold))
        val startedButOnHoldAndStopDateInFuture = create(MedicationStatus.ON_HOLD, evaluationDate.minusDays(1), evaluationDate.plusDays(1))
        assertEquals(MedicationStatusInterpretation.STOPPED, interpreter.interpret(startedButOnHoldAndStopDateInFuture))
        val startedWithUnknownStatus = create(MedicationStatus.UNKNOWN, evaluationDate.minusDays(1), null)
        assertEquals(MedicationStatusInterpretation.UNKNOWN, interpreter.interpret(startedWithUnknownStatus))
        val activeStartedAndNotStopped = create(MedicationStatus.ACTIVE, evaluationDate.minusDays(1), evaluationDate.plusDays(1))
        assertEquals(MedicationStatusInterpretation.ACTIVE, interpreter.interpret(activeStartedAndNotStopped))
        val activeStartedAndStopped = create(MedicationStatus.ACTIVE, evaluationDate.minusDays(2), evaluationDate.minusDays(1))
        assertEquals(MedicationStatusInterpretation.STOPPED, interpreter.interpret(activeStartedAndStopped))
    }

    companion object {
        private fun create(status: MedicationStatus?, startDate: LocalDate?, stopDate: LocalDate?): Medication {
            return TestMedicationFactory.builder().status(status).startDate(startDate).stopDate(stopDate).build()
        }
    }
}