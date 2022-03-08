package com.hartwig.actin.algo.evaluation.medication;

import com.hartwig.actin.PatientRecord;
import com.hartwig.actin.algo.evaluation.util.PassOrFailEvaluator;

import org.jetbrains.annotations.NotNull;

public class CurrentlyGetsMedicationOfExactCategory implements PassOrFailEvaluator {

    @NotNull
    private final String categoryToFind;

    CurrentlyGetsMedicationOfExactCategory(@NotNull final String categoryToFind) {
        this.categoryToFind = categoryToFind;
    }

    @Override
    public boolean isPass(@NotNull PatientRecord record) {
        return !MedicationFilter.withExactCategory(record.clinical().medications(), categoryToFind).isEmpty();
    }

    @NotNull
    @Override
    public String passMessage() {
        return "Patient currently gets medication of category " + categoryToFind;
    }

    @NotNull
    @Override
    public String failMessage() {
        return "Patient currently does not get medication of category " + categoryToFind;
    }
}
