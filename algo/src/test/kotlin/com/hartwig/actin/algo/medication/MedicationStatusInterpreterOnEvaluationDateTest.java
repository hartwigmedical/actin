package com.hartwig.actin.algo.medication;

import static org.junit.Assert.assertEquals;

import java.time.LocalDate;

import com.hartwig.actin.clinical.datamodel.Medication;
import com.hartwig.actin.clinical.datamodel.MedicationStatus;
import com.hartwig.actin.clinical.datamodel.TestMedicationFactory;

import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;
import org.junit.Test;

public class MedicationStatusInterpreterOnEvaluationDateTest {

    @Test
    public void canInterpretMedicationStatus() {
        LocalDate evaluationDate = LocalDate.of(2020, 5, 6);
        MedicationStatusInterpreterOnEvaluationDate interpreter = new MedicationStatusInterpreterOnEvaluationDate(evaluationDate);

        Medication noStatusOrStartOrStop = create(null, null, null);
        assertEquals(MedicationStatusInterpretation.UNKNOWN, interpreter.interpret(noStatusOrStartOrStop));

        Medication cancelled = create(MedicationStatus.CANCELLED, null, null);
        assertEquals(MedicationStatusInterpretation.CANCELLED, interpreter.interpret(cancelled));

        Medication activeWithoutStart = create(MedicationStatus.ACTIVE, null, null);
        assertEquals(MedicationStatusInterpretation.UNKNOWN, interpreter.interpret(activeWithoutStart));

        Medication activeStartingInFuture = create(MedicationStatus.ACTIVE, evaluationDate.plusDays(1), null);
        assertEquals(MedicationStatusInterpretation.PLANNED, interpreter.interpret(activeStartingInFuture));

        Medication activeAndStarted = create(MedicationStatus.ACTIVE, evaluationDate.minusDays(1), null);
        assertEquals(MedicationStatusInterpretation.ACTIVE, interpreter.interpret(activeAndStarted));

        Medication startedButOnHold = create(MedicationStatus.ON_HOLD, evaluationDate.minusDays(1), null);
        assertEquals(MedicationStatusInterpretation.STOPPED, interpreter.interpret(startedButOnHold));

        Medication startedWithUnknownStatus = create(MedicationStatus.UNKNOWN, evaluationDate.minusDays(1), null);
        assertEquals(MedicationStatusInterpretation.UNKNOWN, interpreter.interpret(startedWithUnknownStatus));

        Medication activeStartedAndNotStopped = create(MedicationStatus.ACTIVE, evaluationDate.minusDays(1), evaluationDate.plusDays(1));
        assertEquals(MedicationStatusInterpretation.ACTIVE, interpreter.interpret(activeStartedAndNotStopped));

        Medication activeStartedAndStopped = create(MedicationStatus.ACTIVE, evaluationDate.minusDays(2), evaluationDate.minusDays(1));
        assertEquals(MedicationStatusInterpretation.STOPPED, interpreter.interpret(activeStartedAndStopped));
    }

    @NotNull
    private static Medication create(@Nullable MedicationStatus status, @Nullable LocalDate startDate, @Nullable LocalDate stopDate) {
        return TestMedicationFactory.builder().status(status).startDate(startDate).stopDate(stopDate).build();
    }
}