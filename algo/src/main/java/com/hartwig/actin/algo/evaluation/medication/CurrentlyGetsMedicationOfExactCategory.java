package com.hartwig.actin.algo.evaluation.medication;

import java.util.Set;

import com.hartwig.actin.PatientRecord;
import com.hartwig.actin.algo.evaluation.util.Format;
import com.hartwig.actin.algo.evaluation.util.PassOrFailEvaluator;

import org.jetbrains.annotations.NotNull;

public class CurrentlyGetsMedicationOfExactCategory implements PassOrFailEvaluator {

    @NotNull
    private final Set<String> categoriesToFind;

    CurrentlyGetsMedicationOfExactCategory(@NotNull final Set<String> categoriesToFind) {
        this.categoriesToFind = categoriesToFind;
    }

    @Override
    public boolean isPass(@NotNull PatientRecord record) {
        return !MedicationFilter.withAnyExactCategory(record.clinical().medications(), categoriesToFind).isEmpty();
    }

    @NotNull
    @Override
    public String passMessage() {
        return "Patient currently gets medication of category " + Format.concat(categoriesToFind);
    }

    @NotNull
    @Override
    public String failMessage() {
        return "Patient currently does not get medication of category " + Format.concat(categoriesToFind);
    }
}
