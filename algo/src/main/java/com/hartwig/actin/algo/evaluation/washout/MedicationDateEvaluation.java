package com.hartwig.actin.algo.evaluation.washout;

import java.time.LocalDate;

import com.hartwig.actin.clinical.datamodel.Medication;

import org.jetbrains.annotations.NotNull;

final class MedicationDateEvaluation {

    private MedicationDateEvaluation() {
    }

    public static boolean hasBeenGivenAfterDate(@NotNull Medication medication, @NotNull LocalDate date) {
        LocalDate stopDate = medication.stopDate();
        return stopDate == null || !date.isAfter(stopDate);
    }
}
