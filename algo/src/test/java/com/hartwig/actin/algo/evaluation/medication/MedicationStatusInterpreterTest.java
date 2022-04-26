package com.hartwig.actin.algo.evaluation.medication;

import static org.junit.Assert.assertEquals;

import java.time.LocalDate;

import com.hartwig.actin.clinical.datamodel.Medication;
import com.hartwig.actin.clinical.datamodel.MedicationStatus;

import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;
import org.junit.Test;

public class MedicationStatusInterpreterTest {

    @Test
    public void canInterpretMedicationStatus() {
        LocalDate evaluationDate = LocalDate.of(2020, 5, 6);
        assertEquals(MedicationStatusInterpretation.UNKNOWN,
                MedicationStatusInterpreter.interpret(create(null, null, null), evaluationDate));

        assertEquals(MedicationStatusInterpretation.CANCELLED,
                MedicationStatusInterpreter.interpret(create(MedicationStatus.CANCELLED, null, null), evaluationDate));

        assertEquals(MedicationStatusInterpretation.UNKNOWN,
                MedicationStatusInterpreter.interpret(create(MedicationStatus.ACTIVE, null, null), evaluationDate));

        assertEquals(MedicationStatusInterpretation.PLANNED,
                MedicationStatusInterpreter.interpret(create(MedicationStatus.ACTIVE, evaluationDate.plusDays(1), null), evaluationDate));

        assertEquals(MedicationStatusInterpretation.ACTIVE,
                MedicationStatusInterpreter.interpret(create(MedicationStatus.ACTIVE, evaluationDate.minusDays(1), null), evaluationDate));

        assertEquals(MedicationStatusInterpretation.STOPPED,
                MedicationStatusInterpreter.interpret(create(MedicationStatus.ON_HOLD, evaluationDate.minusDays(1), null), evaluationDate));

        assertEquals(MedicationStatusInterpretation.UNKNOWN,
                MedicationStatusInterpreter.interpret(create(MedicationStatus.UNKNOWN, evaluationDate.minusDays(1), null), evaluationDate));

        assertEquals(MedicationStatusInterpretation.ACTIVE,
                MedicationStatusInterpreter.interpret(create(MedicationStatus.ACTIVE,
                        evaluationDate.minusDays(1),
                        evaluationDate.plusDays(1)), evaluationDate));

        assertEquals(MedicationStatusInterpretation.STOPPED,
                MedicationStatusInterpreter.interpret(create(MedicationStatus.ACTIVE,
                        evaluationDate.minusDays(2),
                        evaluationDate.minusDays(1)), evaluationDate));
    }

    @NotNull
    private static Medication create(@Nullable MedicationStatus status, @Nullable LocalDate startDate, @Nullable LocalDate stopDate) {
        return MedicationTestFactory.builder().status(status).startDate(startDate).stopDate(stopDate).build();
    }

}