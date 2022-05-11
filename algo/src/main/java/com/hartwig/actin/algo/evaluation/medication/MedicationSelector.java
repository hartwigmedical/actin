package com.hartwig.actin.algo.evaluation.medication;

import java.time.LocalDate;
import java.util.List;
import java.util.Set;

import com.google.common.collect.Lists;
import com.google.common.collect.Sets;
import com.hartwig.actin.algo.medication.MedicationStatusInterpretation;
import com.hartwig.actin.algo.medication.MedicationStatusInterpreter;
import com.hartwig.actin.clinical.datamodel.Medication;

import org.jetbrains.annotations.NotNull;

class MedicationSelector {

    @NotNull
    private final MedicationStatusInterpreter interpreter;

    public MedicationSelector(@NotNull final MedicationStatusInterpreter interpreter) {
        this.interpreter = interpreter;
    }

    @NotNull
    public List<Medication> active(@NotNull List<Medication> medications) {
        List<Medication> filtered = Lists.newArrayList();
        for (Medication medication : medications) {
            MedicationStatusInterpretation status = interpreter.interpret(medication);
            if (status == MedicationStatusInterpretation.ACTIVE) {
                filtered.add(medication);
            }
        }
        return filtered;
    }

    @NotNull
    public List<Medication> activeWithAnyTermInName(@NotNull List<Medication> medications, @NotNull Set<String> termsToFind) {
        List<Medication> filtered = Lists.newArrayList();
        for (Medication medication : active(medications)) {
            boolean isMatch = false;
            for (String termToFind : termsToFind) {
                if (medication.name().toLowerCase().contains(termToFind.toLowerCase())) {
                    isMatch = true;
                }
            }
            if (isMatch) {
                filtered.add(medication);
            }
        }
        return filtered;
    }

    @NotNull
    public List<Medication> activeWithExactCategory(@NotNull List<Medication> medications, @NotNull String categoryToFind) {
        return activeWithAnyExactCategory(medications, Sets.newHashSet(categoryToFind));
    }

    @NotNull
    public List<Medication> activeWithAnyExactCategory(@NotNull List<Medication> medications, @NotNull Set<String> categoriesToFind) {
        List<Medication> filtered = Lists.newArrayList();
        for (Medication medication : active(medications)) {
            boolean isMatch = false;
            for (String category : medication.categories()) {
                for (String categoryToFind : categoriesToFind) {
                    if (category.equalsIgnoreCase(categoryToFind)) {
                        isMatch = true;
                        break;
                    }
                }
            }
            if (isMatch) {
                filtered.add(medication);
            }
        }
        return filtered;
    }

    @NotNull
    public List<Medication> activeOrRecentlyStoppedWithCategory(@NotNull List<Medication> medications, @NotNull String categoryToFind,
            @NotNull LocalDate minStopDate) {
        List<Medication> filtered = Lists.newArrayList();
        for (Medication medication : medications) {
            boolean isMatch = false;
            for (String category : medication.categories()) {
                if (category.equalsIgnoreCase(categoryToFind)) {
                    boolean isActive = interpreter.interpret(medication) == MedicationStatusInterpretation.ACTIVE;
                    boolean isRecentlyStopped = medication.stopDate() != null && medication.stopDate().isAfter(minStopDate);
                    if (isActive || isRecentlyStopped) {
                        isMatch = true;
                    }
                }
            }
            if (isMatch) {
                filtered.add(medication);
            }
        }
        return filtered;
    }
}
