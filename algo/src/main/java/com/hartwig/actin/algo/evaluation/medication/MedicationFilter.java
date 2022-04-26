package com.hartwig.actin.algo.evaluation.medication;

import java.time.LocalDate;
import java.util.List;
import java.util.Set;

import com.google.common.collect.Lists;
import com.google.common.collect.Sets;
import com.hartwig.actin.clinical.datamodel.Medication;

import org.jetbrains.annotations.NotNull;

final class MedicationFilter {

    private MedicationFilter() {
    }

    @NotNull
    public static List<Medication> active(@NotNull List<Medication> medications, @NotNull LocalDate evaluationDate) {
        List<Medication> filtered = Lists.newArrayList();
        for (Medication medication : medications) {
            MedicationStatusInterpretation status = MedicationStatusInterpreter.interpret(medication, evaluationDate);
            if (status == MedicationStatusInterpretation.ACTIVE) {
                filtered.add(medication);
            }
        }
        return filtered;
    }

    @NotNull
    public static List<Medication> withTermInName(@NotNull List<Medication> medications, @NotNull LocalDate evaluationDate,
            @NotNull String termToFind) {
        return withAnyTermInName(medications, evaluationDate, Sets.newHashSet(termToFind));
    }

    @NotNull
    public static List<Medication> withAnyTermInName(@NotNull List<Medication> medications, @NotNull LocalDate evaluationDate,
            @NotNull Set<String> termsToFind) {
        List<Medication> filtered = Lists.newArrayList();
        for (Medication medication : active(medications, evaluationDate)) {
            for (String termToFind : termsToFind) {
                if (medication.name().toLowerCase().contains(termToFind.toLowerCase())) {
                    filtered.add(medication);
                }
            }
        }
        return filtered;
    }

    @NotNull
    public static List<Medication> withExactCategory(@NotNull List<Medication> medications, @NotNull LocalDate evaluationDate,
            @NotNull String categoryToFind) {
        return withAnyExactCategory(medications, evaluationDate, Sets.newHashSet(categoryToFind));
    }

    @NotNull
    public static List<Medication> withAnyExactCategory(@NotNull List<Medication> medications, @NotNull LocalDate evaluationDate,
            @NotNull Set<String> categoriesToFind) {
        List<Medication> filtered = Lists.newArrayList();
        for (Medication medication : active(medications, evaluationDate)) {
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
