package com.hartwig.actin.algo.evaluation.medication;

import java.util.List;

import javax.annotation.Nullable;

import com.google.common.collect.Lists;
import com.hartwig.actin.PatientRecord;
import com.hartwig.actin.algo.datamodel.Evaluation;
import com.hartwig.actin.algo.evaluation.EvaluationFunction;
import com.hartwig.actin.clinical.datamodel.Medication;

import org.jetbrains.annotations.NotNull;

public class CurrentlyGetsMedicationWithType implements EvaluationFunction {

    @Nullable
    private final String type;
    private final boolean requireStableDosing;

    CurrentlyGetsMedicationWithType(@Nullable final String type, final boolean requireStableDosing) {
        this.type = type;
        this.requireStableDosing = requireStableDosing;
    }

    @NotNull
    @Override
    public Evaluation evaluate(@NotNull PatientRecord record) {
        boolean hasActiveAndStableMedication = false;
        Medication referenceDosing = null;
        for (Medication medication : filter(record.clinical().medications(), type)) {
            if (referenceDosing != null) {
                if (requireStableDosing && !hasMatchingDosing(medication, referenceDosing)) {
                    hasActiveAndStableMedication = false;
                }
            } else {
                hasActiveAndStableMedication = true;
                referenceDosing = medication;
            }
        }

        return hasActiveAndStableMedication ? Evaluation.PASS : Evaluation.FAIL;
    }

    @NotNull
    private static List<Medication> filter(@NotNull List<Medication> medications, @Nullable String type) {
        List<Medication> filtered = Lists.newArrayList();
        for (Medication medication : medications) {
            Boolean active = medication.active();
            if (active != null && active && (type == null || medication.type().equals(type))) {
                filtered.add(medication);
            }
        }
        return filtered;
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