package com.hartwig.actin.clinical.interpretation

import com.hartwig.actin.datamodel.clinical.Medication
import com.hartwig.actin.datamodel.clinical.MedicationStatus
import com.hartwig.actin.datamodel.clinical.TestMedicationFactory
import org.assertj.core.api.Assertions.assertThat
import org.junit.Test
import java.time.LocalDate

class MedicationStatusInterpreterOnEvaluationDateTest {

    private val evaluationDate = LocalDate.of(2020, 5, 6)
    private val referenceDate = LocalDate.of(2024, 1, 1)
    private val interpreterWithoutReferenceDate = MedicationStatusInterpreterOnEvaluationDate(evaluationDate, null)
    private val interpreterWithReferenceDate = MedicationStatusInterpreterOnEvaluationDate(evaluationDate, referenceDate)

    @Test
    fun `Should always interpret medication status as unknown when no start date provided`() {
        listOf(MedicationStatus.ACTIVE, MedicationStatus.CANCELLED, MedicationStatus.ON_HOLD, MedicationStatus.UNKNOWN, null)
            .forEach(::assertMedicationStatusUnknownWithNoStartDate)
    }

    @Test
    fun `Should interpret medication with active status`() {
        val activeStartedAndNoStopDate = create(MedicationStatus.ACTIVE, evaluationDate.minusDays(1), null)
        assertThat(interpreterWithoutReferenceDate.interpret(activeStartedAndNoStopDate)).isEqualTo(MedicationStatusInterpretation.ACTIVE)
        assertThat(interpreterWithReferenceDate.interpret(activeStartedAndNoStopDate)).isEqualTo(MedicationStatusInterpretation.ACTIVE)
        val activeStartedAndStopped = create(MedicationStatus.ACTIVE, evaluationDate.minusDays(2), evaluationDate.minusDays(1))
        assertThat(interpreterWithoutReferenceDate.interpret(activeStartedAndStopped)).isEqualTo(MedicationStatusInterpretation.STOPPED)
        assertThat(interpreterWithReferenceDate.interpret(activeStartedAndStopped)).isEqualTo(MedicationStatusInterpretation.STOPPED)
        val activeStartedAndNotStopped = create(MedicationStatus.ACTIVE, evaluationDate.minusDays(1), evaluationDate.plusDays(2))
        assertThat(interpreterWithoutReferenceDate.interpret(activeStartedAndNotStopped)).isEqualTo(MedicationStatusInterpretation.ACTIVE)
        assertThat(interpreterWithReferenceDate.interpret(activeStartedAndNotStopped)).isEqualTo(MedicationStatusInterpretation.ACTIVE)

        assertInterpretationForFutureMedication(
            interpreterWithoutReferenceDate,
            MedicationStatus.ACTIVE,
            MedicationStatusInterpretation.PLANNED
        )
        assertInterpretationForFutureMedication(
            interpreterWithReferenceDate,
            MedicationStatus.ACTIVE,
            MedicationStatusInterpretation.PLANNED
        )
    }

    @Test
    fun `Should interpret medication with cancelled status`() {
        assertInterpretationForStartedMedication(
            interpreterWithoutReferenceDate,
            MedicationStatus.CANCELLED,
            MedicationStatusInterpretation.CANCELLED
        )
        assertInterpretationForStartedMedication(
            interpreterWithReferenceDate,
            MedicationStatus.CANCELLED,
            MedicationStatusInterpretation.CANCELLED
        )
        assertInterpretationForFutureMedication(
            interpreterWithoutReferenceDate,
            MedicationStatus.CANCELLED,
            MedicationStatusInterpretation.CANCELLED
        )
        assertInterpretationForFutureMedication(
            interpreterWithReferenceDate,
            MedicationStatus.CANCELLED,
            MedicationStatusInterpretation.CANCELLED
        )
    }

    @Test
    fun `Should interpret medication with on hold status`() {
        assertInterpretationForStartedMedication(
            interpreterWithoutReferenceDate,
            MedicationStatus.ON_HOLD,
            MedicationStatusInterpretation.STOPPED
        )
        assertInterpretationForStartedMedication(
            interpreterWithReferenceDate,
            MedicationStatus.ON_HOLD,
            MedicationStatusInterpretation.ACTIVE,
            MedicationStatusInterpretation.STOPPED
        )
        assertInterpretationForFutureMedication(
            interpreterWithoutReferenceDate,
            MedicationStatus.ON_HOLD,
            MedicationStatusInterpretation.STOPPED
        )
        assertInterpretationForFutureMedication(
            interpreterWithReferenceDate,
            MedicationStatus.ON_HOLD,
            MedicationStatusInterpretation.STOPPED
        )
    }

    @Test
    fun `Should interpret medication with unknown status`() {
        assertInterpretationForStartedMedication(
            interpreterWithoutReferenceDate,
            MedicationStatus.UNKNOWN,
            MedicationStatusInterpretation.UNKNOWN
        )
        assertInterpretationForStartedMedication(
            interpreterWithReferenceDate,
            MedicationStatus.UNKNOWN,
            MedicationStatusInterpretation.UNKNOWN
        )
        assertInterpretationForFutureMedication(
            interpreterWithoutReferenceDate,
            MedicationStatus.UNKNOWN,
            MedicationStatusInterpretation.UNKNOWN
        )
        assertInterpretationForFutureMedication(
            interpreterWithReferenceDate,
            MedicationStatus.UNKNOWN,
            MedicationStatusInterpretation.UNKNOWN
        )
    }

    @Test
    fun `Should interpret medication without status`() {
        val noStatusStartedAndNoStopDate = create(null, evaluationDate.minusDays(1), null)
        assertThat(interpreterWithoutReferenceDate.interpret(noStatusStartedAndNoStopDate)).isEqualTo(MedicationStatusInterpretation.ACTIVE)
        assertThat(interpreterWithReferenceDate.interpret(noStatusStartedAndNoStopDate)).isEqualTo(MedicationStatusInterpretation.ACTIVE)
        val noStatusStartedAndStopped = create(null, evaluationDate.minusDays(2), evaluationDate.minusDays(1))
        assertThat(interpreterWithoutReferenceDate.interpret(noStatusStartedAndStopped)).isEqualTo(MedicationStatusInterpretation.STOPPED)
        assertThat(interpreterWithReferenceDate.interpret(noStatusStartedAndStopped)).isEqualTo(MedicationStatusInterpretation.STOPPED)
        val noStatusStartedAndNotStopped = create(null, evaluationDate.minusDays(1), evaluationDate.plusDays(2))
        assertThat(interpreterWithoutReferenceDate.interpret(noStatusStartedAndNotStopped)).isEqualTo(MedicationStatusInterpretation.ACTIVE)
        assertThat(interpreterWithReferenceDate.interpret(noStatusStartedAndNotStopped)).isEqualTo(MedicationStatusInterpretation.ACTIVE)

        assertInterpretationForFutureMedication(interpreterWithoutReferenceDate, null, MedicationStatusInterpretation.PLANNED)
        assertInterpretationForFutureMedication(interpreterWithReferenceDate, null, MedicationStatusInterpretation.PLANNED)
    }

    private fun assertMedicationStatusUnknownWithNoStartDate(providedStatus: MedicationStatus?) {
        listOf(null, evaluationDate.minusDays(1), evaluationDate.plusDays(1)).forEach { stopDate ->
            val medication = create(providedStatus, null, stopDate)
            assertThat(interpreterWithoutReferenceDate.interpret(medication)).isEqualTo(MedicationStatusInterpretation.UNKNOWN)
            assertThat(interpreterWithReferenceDate.interpret(medication)).isEqualTo(MedicationStatusInterpretation.UNKNOWN)
        }
    }

    private fun assertInterpretationForStartedMedication(
        interpreter: MedicationStatusInterpreterOnEvaluationDate,
        providedStatus: MedicationStatus,
        expectedStatus: MedicationStatusInterpretation,
        startedAndStoppedStatus: MedicationStatusInterpretation = expectedStatus
    ) {
        val startedAndNoStopDate = create(providedStatus, evaluationDate.minusDays(1), null)
        assertThat(interpreter.interpret(startedAndNoStopDate)).isEqualTo(expectedStatus)
        val startedAndStopped = create(providedStatus, evaluationDate.minusDays(2), evaluationDate.minusDays(1))
        assertThat(interpreter.interpret(startedAndStopped)).isEqualTo(startedAndStoppedStatus)
        val startedAndNotStopped = create(providedStatus, evaluationDate.minusDays(1), evaluationDate.plusDays(2))
        assertThat(interpreter.interpret(startedAndNotStopped)).isEqualTo(expectedStatus)
    }

    private fun assertInterpretationForFutureMedication(
        interpreter: MedicationStatusInterpreterOnEvaluationDate,
        providedStatus: MedicationStatus?,
        expectedStatus: MedicationStatusInterpretation
    ) {
        val startingInFutureAndNoStopDate = create(providedStatus, referenceDate.plusDays(1), null)
        assertThat(interpreter.interpret(startingInFutureAndNoStopDate)).isEqualTo(expectedStatus)
        val startingInFutureAndNotStopped = create(providedStatus, referenceDate.plusDays(1), referenceDate.plusDays(2))
        assertThat(interpreter.interpret(startingInFutureAndNotStopped)).isEqualTo(expectedStatus)
    }

    private fun create(status: MedicationStatus?, startDate: LocalDate?, stopDate: LocalDate?): Medication {
        return TestMedicationFactory.createMinimal().copy(status = status, startDate = startDate, stopDate = stopDate)
    }
}