package com.hartwig.actin.algo.evaluation.medication;

import com.hartwig.actin.PatientRecord;
import com.hartwig.actin.algo.evaluation.util.PassOrFailEvaluator;
import com.hartwig.actin.clinical.datamodel.Medication;

import org.jetbrains.annotations.NotNull;

public class CurrentlyGetsMedicationOfApproximateCategory implements PassOrFailEvaluator {

    @NotNull
    private final String categoryTermToFind;

    CurrentlyGetsMedicationOfApproximateCategory(@NotNull final String categoryTermToFind) {
        this.categoryTermToFind = categoryTermToFind;
    }

    @Override
    public boolean isPass(@NotNull PatientRecord record) {
        for (Medication medication : MedicationFilter.active(record.clinical().medications())) {
            for (String category : medication.categories()) {
                if (category.toLowerCase().contains(categoryTermToFind.toLowerCase())) {
                    return true;
                }
            }
        }

        return false;
    }

    @NotNull
    @Override
    public String passMessage() {
        return "Patient currently gets medication of category " + categoryTermToFind;
    }

    @NotNull
    @Override
    public String failMessage() {
        return "Patient currently does not get medication of category " + categoryTermToFind;
    }
}
