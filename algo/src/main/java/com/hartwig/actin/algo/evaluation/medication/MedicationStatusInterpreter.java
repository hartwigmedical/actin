package com.hartwig.actin.algo.evaluation.medication;

import java.time.LocalDate;

import com.hartwig.actin.clinical.datamodel.Medication;
import com.hartwig.actin.clinical.datamodel.MedicationStatus;

import org.jetbrains.annotations.NotNull;

public final class MedicationStatusInterpreter {

    private MedicationStatusInterpreter() {
    }

    @NotNull
    public static MedicationStatusInterpretation interpret(@NotNull Medication medication, @NotNull LocalDate evaluationDate) {
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
