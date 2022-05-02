package com.hartwig.actin.algo.medication;

import java.time.LocalDate;

import com.hartwig.actin.clinical.datamodel.Medication;
import com.hartwig.actin.clinical.datamodel.MedicationStatus;

import org.jetbrains.annotations.NotNull;

public class MedicationStatusInterpreterOnEvaluationDate implements MedicationStatusInterpreter {

    @NotNull
    private final LocalDate evaluationDate;

    public MedicationStatusInterpreterOnEvaluationDate(@NotNull final LocalDate evaluationDate) {
        this.evaluationDate = evaluationDate;
    }

    @NotNull
    @Override
    public MedicationStatusInterpretation interpret(@NotNull Medication medication) {
        MedicationStatus status = medication.status();
        if (status == null) {
            return MedicationStatusInterpretation.UNKNOWN;
        } else if (status == MedicationStatus.CANCELLED) {
            return MedicationStatusInterpretation.CANCELLED;
        }

        LocalDate startDate = medication.startDate();

        if (startDate == null) {
            return MedicationStatusInterpretation.UNKNOWN;
        }

        boolean startIsBeforeEvaluation = startDate.isBefore(evaluationDate);
        if (!startIsBeforeEvaluation) {
            return MedicationStatusInterpretation.PLANNED;
        } else {
            LocalDate stopDate = medication.stopDate();
            if (stopDate == null) {
                if (status == MedicationStatus.ON_HOLD) {
                    return MedicationStatusInterpretation.STOPPED;
                } else if (status == MedicationStatus.ACTIVE) {
                    return MedicationStatusInterpretation.ACTIVE;
                } else {
                    return MedicationStatusInterpretation.UNKNOWN;
                }
            } else {
                boolean stopIsBeforeEvaluation = stopDate.isBefore(evaluationDate);
                return stopIsBeforeEvaluation ? MedicationStatusInterpretation.STOPPED : MedicationStatusInterpretation.ACTIVE;
            }
        }
    }
}
