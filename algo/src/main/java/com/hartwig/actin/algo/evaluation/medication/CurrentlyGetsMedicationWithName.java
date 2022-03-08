package com.hartwig.actin.algo.evaluation.medication;

import java.util.Set;
import java.util.StringJoiner;

import com.hartwig.actin.PatientRecord;
import com.hartwig.actin.algo.evaluation.util.PassOrFailEvaluator;
import com.hartwig.actin.clinical.datamodel.Medication;

import org.jetbrains.annotations.NotNull;

public class CurrentlyGetsMedicationWithName implements PassOrFailEvaluator {

    @NotNull
    private final Set<String> termsToFind;

    public CurrentlyGetsMedicationWithName(@NotNull final Set<String> termsToFind) {
        this.termsToFind = termsToFind;
    }

    @Override
    public boolean isPass(@NotNull PatientRecord record) {
        for (Medication medication : MedicationFilter.active(record.clinical().medications())) {
            for (String termToFind : termsToFind) {
                if (medication.name().toLowerCase().contains(termToFind.toLowerCase())) {
                    return true;
                }
            }
        }

        return false;
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
