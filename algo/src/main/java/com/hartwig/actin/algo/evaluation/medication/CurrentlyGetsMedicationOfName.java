package com.hartwig.actin.algo.evaluation.medication;

import java.util.Set;
import java.util.StringJoiner;

import com.hartwig.actin.PatientRecord;
import com.hartwig.actin.algo.evaluation.util.PassOrFailEvaluator;

import org.jetbrains.annotations.NotNull;

public class CurrentlyGetsMedicationOfName implements PassOrFailEvaluator {

    @NotNull
    private final Set<String> termsToFind;

    public CurrentlyGetsMedicationOfName(@NotNull final Set<String> termsToFind) {
        this.termsToFind = termsToFind;
    }

    @Override
    public boolean isPass(@NotNull PatientRecord record) {
        return !MedicationFilter.withAnyTermInName(record.clinical().medications(), termsToFind).isEmpty();
    }

    @NotNull
    @Override
    public String passMessage() {
        return "Patient currently gets medication with name " + concat(termsToFind);
    }

    @NotNull
    @Override
    public String failMessage() {
        return "Patient currently does not get medication with name " + concat(termsToFind);
    }

    @NotNull
    private static String concat(@NotNull Set<String> strings) {
        StringJoiner joiner = new StringJoiner("; ");
        for (String string : strings) {
            joiner.add(string);
        }
        return joiner.toString();
    }
}
