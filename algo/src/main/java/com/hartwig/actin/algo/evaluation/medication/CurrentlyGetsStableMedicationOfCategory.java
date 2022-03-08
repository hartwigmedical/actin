package com.hartwig.actin.algo.evaluation.medication;

import com.hartwig.actin.PatientRecord;
import com.hartwig.actin.algo.evaluation.util.PassOrFailEvaluator;
import com.hartwig.actin.clinical.datamodel.Medication;

import org.jetbrains.annotations.NotNull;

public class CurrentlyGetsStableMedicationOfCategory implements PassOrFailEvaluator {

    @NotNull
    private final String categoryToFind;

    CurrentlyGetsStableMedicationOfCategory(@NotNull final String categoryToFind) {
        this.categoryToFind = categoryToFind;
    }

    @Override
    public boolean isPass(@NotNull PatientRecord record) {
        boolean hasActiveAndStableMedication = false;
        Medication referenceDosing = null;
        for (Medication medication : MedicationFilter.withExactCategory(record.clinical().medications(), categoryToFind)) {
            if (referenceDosing != null) {
                if (!hasMatchingDosing(medication, referenceDosing)) {
                    hasActiveAndStableMedication = false;
                }
            } else {
                hasActiveAndStableMedication = true;
                referenceDosing = medication;
            }
        }

        return hasActiveAndStableMedication;
    }

    @NotNull
    @Override
    public String passMessage() {
        return "Patient gets stable dosing of medication with category " + categoryToFind;
    }

    @NotNull
    @Override
    public String failMessage() {
        return "Patient does not get stable dosing of medication with category " + categoryToFind;
    }

    private static boolean hasMatchingDosing(@NotNull Medication medication1, @NotNull Medication medication2) {
        if (hasDosing(medication1) && hasDosing(medication2)) {
            boolean dosageMinMatch = Double.compare(medication1.dosageMin(), medication2.dosageMin()) == 0;
            boolean dosageMaxMatch = Double.compare(medication1.dosageMax(), medication2.dosageMax()) == 0;
            boolean dosageUnitMatch = medication1.dosageUnit().equals(medication2.dosageUnit());
            boolean frequencyMatch = Double.compare(medication1.frequency(), medication2.frequency()) == 0;
            boolean frequencyUnitMatch = medication1.frequencyUnit().equals(medication2.frequencyUnit());
            boolean ifNeededMatch = medication1.ifNeeded() == medication2.ifNeeded();

            return dosageMinMatch && dosageMaxMatch && dosageUnitMatch && frequencyMatch && frequencyUnitMatch && ifNeededMatch;
        } else {
            return false;
        }
    }

    private static boolean hasDosing(@NotNull Medication medication) {
        return medication.dosageMin() != null && medication.dosageMax() != null && medication.dosageUnit() != null
                && medication.frequency() != null && medication.frequencyUnit() != null && medication.ifNeeded() != null;
    }
}
