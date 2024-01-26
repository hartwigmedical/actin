package com.hartwig.actin.algo.medication

import com.hartwig.actin.clinical.datamodel.Medication
import com.hartwig.actin.clinical.datamodel.MedicationStatus
import com.hartwig.actin.clinical.datamodel.TestMedicationFactory
import org.junit.Assert.assertEquals
import org.junit.Test
import java.time.LocalDate

class MedicationStatusInterpreterOnEvaluationDateTest {

    private val evaluationDate = LocalDate.of(2020, 5, 6)
    private val interpreter = MedicationStatusInterpreterOnEvaluationDate(evaluationDate)

    @Test
    fun canInterpretMedicationWithActiveStatus() {
        val activeNoStartAndStopDate = create(MedicationStatus.ACTIVE, null, null)
        assertEquals(MedicationStatusInterpretation.UNKNOWN, interpreter.interpret(activeNoStartAndStopDate))
        val activeNoStartDateAndStopped = create(MedicationStatus.ACTIVE, null, evaluationDate.minusDays(1))
        assertEquals(MedicationStatusInterpretation.UNKNOWN, interpreter.interpret(activeNoStartDateAndStopped))
        val activeNoStartDateAndNotStopped = create(MedicationStatus.ACTIVE, null, evaluationDate.plusDays(1))
        assertEquals(MedicationStatusInterpretation.UNKNOWN, interpreter.interpret(activeNoStartDateAndNotStopped))

        val activeStartedAndNoStopDate = create(MedicationStatus.ACTIVE, evaluationDate.minusDays(1), null)
        assertEquals(MedicationStatusInterpretation.ACTIVE, interpreter.interpret(activeStartedAndNoStopDate))
        val activeStartedAndStopped = create(MedicationStatus.ACTIVE, evaluationDate.minusDays(2), evaluationDate.minusDays(1))
        assertEquals(MedicationStatusInterpretation.STOPPED, interpreter.interpret(activeStartedAndStopped))
        val activeStartedAndNotStopped = create(MedicationStatus.ACTIVE, evaluationDate.minusDays(1), evaluationDate.plusDays(2))
        assertEquals(MedicationStatusInterpretation.ACTIVE, interpreter.interpret(activeStartedAndNotStopped))

        val activeStartingInFutureAndNoStopDate = create(MedicationStatus.ACTIVE, evaluationDate.plusDays(1), null)
        assertEquals(MedicationStatusInterpretation.PLANNED, interpreter.interpret(activeStartingInFutureAndNoStopDate))
        val activeStartingInFutureAndNotStopped = create(MedicationStatus.ACTIVE, evaluationDate.plusDays(1), evaluationDate.plusDays(2))
        assertEquals(MedicationStatusInterpretation.PLANNED, interpreter.interpret(activeStartingInFutureAndNotStopped))
    }

    @Test
    fun canInterpretMedicationWithCancelledStatus() {
        val cancelledNoStartAndStopDate = create(MedicationStatus.CANCELLED, null, null)
        assertEquals(MedicationStatusInterpretation.UNKNOWN, interpreter.interpret(cancelledNoStartAndStopDate))
        val cancelledNoStartDateAndStopped = create(MedicationStatus.CANCELLED, null, evaluationDate.minusDays(1))
        assertEquals(MedicationStatusInterpretation.UNKNOWN, interpreter.interpret(cancelledNoStartDateAndStopped))
        val cancelledNoStartDateAndNotStopped = create(MedicationStatus.CANCELLED, null, evaluationDate.plusDays(1))
        assertEquals(MedicationStatusInterpretation.UNKNOWN, interpreter.interpret(cancelledNoStartDateAndNotStopped))

        val cancelledStartedAndNoStopDate = create(MedicationStatus.CANCELLED, evaluationDate.minusDays(1), null)
        assertEquals(MedicationStatusInterpretation.CANCELLED, interpreter.interpret(cancelledStartedAndNoStopDate))
        val cancelledStartedAndStopped = create(MedicationStatus.CANCELLED, evaluationDate.minusDays(2), evaluationDate.minusDays(1))
        assertEquals(MedicationStatusInterpretation.CANCELLED, interpreter.interpret(cancelledStartedAndStopped))
        val cancelledStartedAndNotStopped = create(MedicationStatus.CANCELLED, evaluationDate.minusDays(1), evaluationDate.plusDays(2))
        assertEquals(MedicationStatusInterpretation.CANCELLED, interpreter.interpret(cancelledStartedAndNotStopped))

        val cancelledStartingInFutureAndNoStopDate = create(MedicationStatus.CANCELLED, evaluationDate.plusDays(1), null)
        assertEquals(MedicationStatusInterpretation.CANCELLED, interpreter.interpret(cancelledStartingInFutureAndNoStopDate))
        val cancelledStartingInFutureAndNotStopped = create(MedicationStatus.CANCELLED, evaluationDate.plusDays(1), evaluationDate.plusDays(2))
        assertEquals(MedicationStatusInterpretation.CANCELLED, interpreter.interpret(cancelledStartingInFutureAndNotStopped))
    }

    @Test
    fun canInterpretMedicationWithOnHoldStatus() {
        val onHoldNoStartAndStopDate = create(MedicationStatus.ON_HOLD, null, null)
        assertEquals(MedicationStatusInterpretation.UNKNOWN, interpreter.interpret(onHoldNoStartAndStopDate))
        val onHoldNoStartDateAndStopped = create(MedicationStatus.ON_HOLD, null, evaluationDate.minusDays(1))
        assertEquals(MedicationStatusInterpretation.UNKNOWN, interpreter.interpret(onHoldNoStartDateAndStopped))
        val onHoldNoStartDateAndNotStopped = create(MedicationStatus.ON_HOLD, null, evaluationDate.plusDays(1))
        assertEquals(MedicationStatusInterpretation.UNKNOWN, interpreter.interpret(onHoldNoStartDateAndNotStopped))

        val onHoldStartedAndNoStopDate = create(MedicationStatus.ON_HOLD, evaluationDate.minusDays(1), null)
        assertEquals(MedicationStatusInterpretation.STOPPED, interpreter.interpret(onHoldStartedAndNoStopDate))
        val onHoldStartedAndStopped = create(MedicationStatus.ON_HOLD, evaluationDate.minusDays(2), evaluationDate.minusDays(1))
        assertEquals(MedicationStatusInterpretation.STOPPED, interpreter.interpret(onHoldStartedAndStopped))
        val onHoldStartedAndNotStopped = create(MedicationStatus.ON_HOLD, evaluationDate.minusDays(1), evaluationDate.plusDays(2))
        assertEquals(MedicationStatusInterpretation.STOPPED, interpreter.interpret(onHoldStartedAndNotStopped))

        val onHoldStartingInFutureAndNoStopDate = create(MedicationStatus.ON_HOLD, evaluationDate.plusDays(1), null)
        assertEquals(MedicationStatusInterpretation.STOPPED, interpreter.interpret(onHoldStartingInFutureAndNoStopDate))
        val onHoldStartingInFutureAndNotStopped = create(MedicationStatus.ON_HOLD, evaluationDate.plusDays(1), evaluationDate.plusDays(2))
        assertEquals(MedicationStatusInterpretation.STOPPED, interpreter.interpret(onHoldStartingInFutureAndNotStopped))
    }

    @Test
    fun canInterpretMedicationWithUnknownStatus(){
        val unknownStatusNoStartAndStopDate = create(MedicationStatus.UNKNOWN, null, null)
        assertEquals(MedicationStatusInterpretation.UNKNOWN, interpreter.interpret(unknownStatusNoStartAndStopDate))
        val unknownStatusNoStartDateAndStopped = create(MedicationStatus.UNKNOWN, null, evaluationDate.minusDays(1))
        assertEquals(MedicationStatusInterpretation.UNKNOWN, interpreter.interpret(unknownStatusNoStartDateAndStopped))
        val unknownStatusNoStartDateAndNotStopped = create(MedicationStatus.UNKNOWN, null, evaluationDate.plusDays(1))
        assertEquals(MedicationStatusInterpretation.UNKNOWN, interpreter.interpret(unknownStatusNoStartDateAndNotStopped))

        val unknownStatusStartedAndNoStopDate = create(MedicationStatus.UNKNOWN, evaluationDate.minusDays(1), null)
        assertEquals(MedicationStatusInterpretation.UNKNOWN, interpreter.interpret(unknownStatusStartedAndNoStopDate))
        val unknownStatusStartedAndStopped = create(MedicationStatus.UNKNOWN, evaluationDate.minusDays(2), evaluationDate.minusDays(1))
        assertEquals(MedicationStatusInterpretation.UNKNOWN, interpreter.interpret(unknownStatusStartedAndStopped))
        val unknownStatusStartedAndNotStopped = create(MedicationStatus.UNKNOWN, evaluationDate.minusDays(1), evaluationDate.plusDays(2))
        assertEquals(MedicationStatusInterpretation.UNKNOWN, interpreter.interpret(unknownStatusStartedAndNotStopped))

        val unknownStatusStartingInFutureAndNoStopDate = create(MedicationStatus.UNKNOWN, evaluationDate.plusDays(1), null)
        assertEquals(MedicationStatusInterpretation.UNKNOWN, interpreter.interpret(unknownStatusStartingInFutureAndNoStopDate))
        val unknownStatusStartingInFutureAndNotStopped = create(MedicationStatus.UNKNOWN, evaluationDate.plusDays(1), evaluationDate.plusDays(2))
        assertEquals(MedicationStatusInterpretation.UNKNOWN, interpreter.interpret(unknownStatusStartingInFutureAndNotStopped))
    }

    @Test
    fun canInterpretMedicationWithoutStatus() {
        val noStatusNoStartAndStopDate = create(null, null, null)
        assertEquals(MedicationStatusInterpretation.UNKNOWN, interpreter.interpret(noStatusNoStartAndStopDate))
        val noStatusNoStartDateAndStopped = create(null, null, evaluationDate.minusDays(1))
        assertEquals(MedicationStatusInterpretation.UNKNOWN, interpreter.interpret(noStatusNoStartDateAndStopped))
        val noStatusNoStartDateAndNotStopped = create(null, null, evaluationDate.plusDays(1))
        assertEquals(MedicationStatusInterpretation.UNKNOWN, interpreter.interpret(noStatusNoStartDateAndNotStopped))

        val noStatusStartedAndNoStopDate = create(null, evaluationDate.minusDays(1), null)
        assertEquals(MedicationStatusInterpretation.ACTIVE, interpreter.interpret(noStatusStartedAndNoStopDate))
        val noStatusStartedAndStopped = create(null, evaluationDate.minusDays(2), evaluationDate.minusDays(1))
        assertEquals(MedicationStatusInterpretation.STOPPED, interpreter.interpret(noStatusStartedAndStopped))
        val noStatusStartedAndNotStopped = create(null, evaluationDate.minusDays(1), evaluationDate.plusDays(2))
        assertEquals(MedicationStatusInterpretation.ACTIVE, interpreter.interpret(noStatusStartedAndNotStopped))

        val noStatusStartingInFutureAndNoStopDate = create(null, evaluationDate.plusDays(1), null)
        assertEquals(MedicationStatusInterpretation.PLANNED, interpreter.interpret(noStatusStartingInFutureAndNoStopDate))
        val noStatusStartingInFutureAndNotStopped = create(null, evaluationDate.plusDays(1), evaluationDate.plusDays(2))
        assertEquals(MedicationStatusInterpretation.PLANNED, interpreter.interpret(noStatusStartingInFutureAndNotStopped))
    }

    private fun create(status: MedicationStatus?, startDate: LocalDate?, stopDate: LocalDate?): Medication {
        return TestMedicationFactory.builder().status(status).startDate(startDate).stopDate(stopDate).build()
    }
}