package com.hartwig.actin.algo.medication

import com.hartwig.actin.clinical.datamodel.Medication
import com.hartwig.actin.clinical.datamodel.MedicationStatus
import com.hartwig.actin.clinical.datamodel.TestMedicationFactory
import org.assertj.core.api.Assertions.assertThat
import org.junit.Test
import java.time.LocalDate

class MedicationStatusInterpreterOnEvaluationDateTest {

    @Test
    fun canInterpretMedicationStatus() {
        val evaluationDate = LocalDate.of(2020, 5, 6)
        val interpreter = MedicationStatusInterpreterOnEvaluationDate(evaluationDate)
        val noStatusOrStartOrStop = create(null, null, null)
        assertThat(interpreter.interpret(noStatusOrStartOrStop)).isEqualTo(MedicationStatusInterpretation.UNKNOWN)
        
        val cancelled = create(MedicationStatus.CANCELLED, null, null)
        assertThat(interpreter.interpret(cancelled)).isEqualTo(MedicationStatusInterpretation.CANCELLED)
        
        val activeWithoutStart = create(MedicationStatus.ACTIVE, null, null)
        assertThat(interpreter.interpret(activeWithoutStart)).isEqualTo(MedicationStatusInterpretation.UNKNOWN)
        
        val activeStartingInFuture = create(MedicationStatus.ACTIVE, evaluationDate.plusDays(1), null)
        assertThat(interpreter.interpret(activeStartingInFuture)).isEqualTo(MedicationStatusInterpretation.PLANNED)
        
        val activeAndStarted = create(MedicationStatus.ACTIVE, evaluationDate.minusDays(1), null)
        assertThat(interpreter.interpret(activeAndStarted)).isEqualTo(MedicationStatusInterpretation.ACTIVE)
        
        val startedButOnHold = create(MedicationStatus.ON_HOLD, evaluationDate.minusDays(1), null)
        assertThat(interpreter.interpret(startedButOnHold)).isEqualTo(MedicationStatusInterpretation.STOPPED)
        
        val startedWithUnknownStatus = create(MedicationStatus.UNKNOWN, evaluationDate.minusDays(1), null)
        assertThat(interpreter.interpret(startedWithUnknownStatus)).isEqualTo(MedicationStatusInterpretation.UNKNOWN)
        
        val activeStartedAndNotStopped = create(MedicationStatus.ACTIVE, evaluationDate.minusDays(1), evaluationDate.plusDays(1))
        assertThat(interpreter.interpret(activeStartedAndNotStopped)).isEqualTo(MedicationStatusInterpretation.ACTIVE)
        
        val activeStartedAndStopped = create(MedicationStatus.ACTIVE, evaluationDate.minusDays(2), evaluationDate.minusDays(1))
        assertThat(interpreter.interpret(activeStartedAndStopped)).isEqualTo(MedicationStatusInterpretation.STOPPED)
    }

    private fun create(status: MedicationStatus?, startDate: LocalDate?, stopDate: LocalDate?): Medication {
        return TestMedicationFactory.createMinimal().copy(
            status = status, startDate = startDate, stopDate = stopDate,
        )
    }
}