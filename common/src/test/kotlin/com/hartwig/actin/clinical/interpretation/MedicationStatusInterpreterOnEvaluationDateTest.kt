package com.hartwig.actin.clinical.interpretation

import com.hartwig.actin.clinical.datamodel.Medication
import com.hartwig.actin.clinical.datamodel.MedicationStatus
import com.hartwig.actin.clinical.datamodel.TestMedicationFactory
import org.assertj.core.api.Assertions.assertThat
import org.junit.Test
import java.time.LocalDate

class MedicationStatusInterpreterOnEvaluationDateTest {

    private val evaluationDate = LocalDate.of(2020, 5, 6)
    private val interpreter = MedicationStatusInterpreterOnEvaluationDate(evaluationDate)

    @Test
    fun `Should always interpret medication status as unknown when no start date provided`() {
        listOf(MedicationStatus.ACTIVE, MedicationStatus.CANCELLED, MedicationStatus.ON_HOLD, MedicationStatus.UNKNOWN, null)
            .forEach(::assertMedicationStatusUnknownWithNoStartDate)
    }

    @Test
    fun `Should interpret medication with active status`() {
        val activeStartedAndNoStopDate = create(MedicationStatus.ACTIVE, evaluationDate.minusDays(1), null)
        assertThat(interpreter.interpret(activeStartedAndNoStopDate)).isEqualTo(MedicationStatusInterpretation.ACTIVE)
        val activeStartedAndStopped = create(MedicationStatus.ACTIVE, evaluationDate.minusDays(2), evaluationDate.minusDays(1))
        assertThat(interpreter.interpret(activeStartedAndStopped)).isEqualTo(MedicationStatusInterpretation.STOPPED)
        val activeStartedAndNotStopped = create(MedicationStatus.ACTIVE, evaluationDate.minusDays(1), evaluationDate.plusDays(2))
        assertThat(interpreter.interpret(activeStartedAndNotStopped)).isEqualTo(MedicationStatusInterpretation.ACTIVE)

        assertInterpretationForFutureMedication(MedicationStatus.ACTIVE, MedicationStatusInterpretation.PLANNED)
    }

    @Test
    fun `Should interpret medication with cancelled status`() {
        assertInterpretationForStartedMedication(MedicationStatus.CANCELLED, MedicationStatusInterpretation.CANCELLED)
        assertInterpretationForFutureMedication(MedicationStatus.CANCELLED, MedicationStatusInterpretation.CANCELLED)
    }

    @Test
    fun `Should interpret medication with on hold status`() {
        assertInterpretationForStartedMedication(MedicationStatus.ON_HOLD, MedicationStatusInterpretation.STOPPED)
        assertInterpretationForFutureMedication(MedicationStatus.ON_HOLD, MedicationStatusInterpretation.STOPPED)
    }

    @Test
    fun `Should interpret medication with unknown status`() {
        assertInterpretationForStartedMedication(MedicationStatus.UNKNOWN, MedicationStatusInterpretation.UNKNOWN)
        assertInterpretationForFutureMedication(MedicationStatus.UNKNOWN, MedicationStatusInterpretation.UNKNOWN)
    }

    @Test
    fun `Should interpret medication without status`() {
        val noStatusStartedAndNoStopDate = create(null, evaluationDate.minusDays(1), null)
        assertThat(interpreter.interpret(noStatusStartedAndNoStopDate)).isEqualTo(MedicationStatusInterpretation.ACTIVE)
        val noStatusStartedAndStopped = create(null, evaluationDate.minusDays(2), evaluationDate.minusDays(1))
        assertThat(interpreter.interpret(noStatusStartedAndStopped)).isEqualTo(MedicationStatusInterpretation.STOPPED)
        val noStatusStartedAndNotStopped = create(null, evaluationDate.minusDays(1), evaluationDate.plusDays(2))
        assertThat(interpreter.interpret(noStatusStartedAndNotStopped)).isEqualTo(MedicationStatusInterpretation.ACTIVE)

        assertInterpretationForFutureMedication(null, MedicationStatusInterpretation.PLANNED)
    }

    private fun assertMedicationStatusUnknownWithNoStartDate(providedStatus: MedicationStatus?) {
        listOf(null, evaluationDate.minusDays(1), evaluationDate.plusDays(1)).forEach { stopDate ->
            val medication = create(providedStatus, null, stopDate)
            assertThat(interpreter.interpret(medication)).isEqualTo(MedicationStatusInterpretation.UNKNOWN)
        }
    }

    private fun assertInterpretationForStartedMedication(providedStatus: MedicationStatus, expectedStatus: MedicationStatusInterpretation) {
        val startedAndNoStopDate = create(providedStatus, evaluationDate.minusDays(1), null)
        assertThat(interpreter.interpret(startedAndNoStopDate)).isEqualTo(expectedStatus)
        val startedAndStopped = create(providedStatus, evaluationDate.minusDays(2), evaluationDate.minusDays(1))
        assertThat(interpreter.interpret(startedAndStopped)).isEqualTo(expectedStatus)
        val startedAndNotStopped = create(providedStatus, evaluationDate.minusDays(1), evaluationDate.plusDays(2))
        assertThat(interpreter.interpret(startedAndNotStopped)).isEqualTo(expectedStatus)
    }

    private fun assertInterpretationForFutureMedication(providedStatus: MedicationStatus?, expectedStatus: MedicationStatusInterpretation) {
        val startingInFutureAndNoStopDate = create(providedStatus, evaluationDate.plusDays(1), null)
        assertThat(interpreter.interpret(startingInFutureAndNoStopDate)).isEqualTo(expectedStatus)
        val startingInFutureAndNotStopped = create(providedStatus, evaluationDate.plusDays(1), evaluationDate.plusDays(2))
        assertThat(interpreter.interpret(startingInFutureAndNotStopped)).isEqualTo(expectedStatus)
    }

    private fun create(status: MedicationStatus?, startDate: LocalDate?, stopDate: LocalDate?): Medication {
        return TestMedicationFactory.createMinimal().copy(status = status, startDate = startDate, stopDate = stopDate)
    }
}