package com.hartwig.actin.algo.evaluation.medication;

import java.util.List;

import com.google.common.collect.Lists;
import com.hartwig.actin.clinical.datamodel.Medication;

import org.jetbrains.annotations.NotNull;

final class MedicationFilter {

    private MedicationFilter() {
    }

    @NotNull
    public static List<Medication> active(@NotNull List<Medication> medications) {
        List<Medication> filtered = Lists.newArrayList();
        for (Medication medication : medications) {
            Boolean active = medication.active();
            if (active != null && active) {
                filtered.add(medication);
            }
        }
        return filtered;
    }

    @NotNull
    public static List<Medication> withExactCategory(@NotNull List<Medication> medications, @NotNull String categoryToFind) {
        List<Medication> filtered = Lists.newArrayList();
        for (Medication medication : active(medications)) {
            for (String category : medication.categories()) {
                if (category.equalsIgnoreCase(categoryToFind)) {
                    filtered.add(medication);
                }
            }
        }
        return filtered;
    }
}
