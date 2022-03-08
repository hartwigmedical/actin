package com.hartwig.actin.algo.evaluation.medication;

import java.util.Set;
import java.util.StringJoiner;

import com.hartwig.actin.PatientRecord;
import com.hartwig.actin.algo.evaluation.util.PassOrFailEvaluator;
import com.hartwig.actin.clinical.datamodel.Medication;

import org.jetbrains.annotations.NotNull;

public class CurrentlyGetsStableMedicationOfName implements PassOrFailEvaluator {


    @NotNull
    private final Set<String> termsToFind;

    CurrentlyGetsStableMedicationOfName(@NotNull final Set<String> termsToFind) {
        this.termsToFind = termsToFind;
    }

    @Override
    public boolean isPass(@NotNull PatientRecord record) {
        boolean hasFoundOnePassingTerm = false;
        for (String termToFind : termsToFind) {
            boolean hasActiveAndStableMedication = false;
            Medication referenceDosing = null;
            for (Medication medication : MedicationFilter.withTermInName(record.clinical().medications(), termToFind)) {
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
                hasFoundOnePassingTerm = true;
            }
        }

        return hasFoundOnePassingTerm;
    }

    @NotNull
    @Override
    public String passMessage() {
        return "Patient gets stable dosing of medication with name " + concat(termsToFind);
    }

    @NotNull
    @Override
    public String failMessage() {
        return "Patient does not get stable dosing of medication with name " + concat(termsToFind);
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
