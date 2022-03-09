package com.hartwig.actin.algo.evaluation.medication;

import java.util.Set;

import com.hartwig.actin.PatientRecord;
import com.hartwig.actin.algo.evaluation.util.Format;
import com.hartwig.actin.algo.evaluation.util.PassOrFailEvaluator;
import com.hartwig.actin.clinical.datamodel.Medication;

import org.jetbrains.annotations.NotNull;

public class CurrentlyGetsStableMedicationOfCategory implements PassOrFailEvaluator {

    @NotNull
    private final Set<String> categoriesToFind;

    CurrentlyGetsStableMedicationOfCategory(@NotNull final Set<String> categoriesToFind) {
        this.categoriesToFind = categoriesToFind;
    }

    @Override
    public boolean isPass(@NotNull PatientRecord record) {
        boolean hasFoundOnePassingCategory = false;
        for (String categoryToFind : categoriesToFind) {
            boolean hasActiveAndStableMedication = false;
            Medication referenceDosing = null;
            for (Medication medication : MedicationFilter.withExactCategory(record.clinical().medications(), categoryToFind)) {
                if (referenceDosing != null) {
                    if (!MedicationDosage.hasMatchingDosing(medication, referenceDosing)) {
                        hasActiveAndStableMedication = false;
                    }
                } else {
                    hasActiveAndStableMedication = true;
                    referenceDosing = medication;
                }
            }

            if (hasActiveAndStableMedication) {
                hasFoundOnePassingCategory = true;
            }
        }

        return hasFoundOnePassingCategory;
    }

    @NotNull
    @Override
    public String passMessage() {
        return "Patient gets stable dosing of medication with category " + Format.concat(categoriesToFind);
    }

    @NotNull
    @Override
    public String failMessage() {
        return "Patient does not get stable dosing of medication with category " + Format.concat(categoriesToFind);
    }
}
