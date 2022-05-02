package com.hartwig.actin.algo.evaluation.medication;

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
    public List<Medication> withAnyTermInName(@NotNull List<Medication> medications, @NotNull Set<String> termsToFind) {
        List<Medication> filtered = Lists.newArrayList();
        for (Medication medication : active(medications)) {
            for (String termToFind : termsToFind) {
                if (medication.name().toLowerCase().contains(termToFind.toLowerCase())) {
                    filtered.add(medication);
                }
            }
        }
        return filtered;
    }

    @NotNull
    public List<Medication> withExactCategory(@NotNull List<Medication> medications, @NotNull String categoryToFind) {
        return withAnyExactCategory(medications, Sets.newHashSet(categoryToFind));
    }

    @NotNull
    public List<Medication> withAnyExactCategory(@NotNull List<Medication> medications, @NotNull Set<String> categoriesToFind) {
        List<Medication> filtered = Lists.newArrayList();
        for (Medication medication : active(medications)) {
            for (String category : medication.categories()) {
                for (String categoryToFind : categoriesToFind) {
                    if (category.equalsIgnoreCase(categoryToFind)) {
                        filtered.add(medication);
                    }
                }
            }
        }
        return filtered;
    }
}
