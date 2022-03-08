package com.hartwig.actin.algo.evaluation.washout;

import java.time.LocalDate;
import java.util.Set;
import java.util.StringJoiner;

import com.hartwig.actin.PatientRecord;
import com.hartwig.actin.algo.evaluation.util.PassOrFailEvaluator;
import com.hartwig.actin.clinical.datamodel.Medication;

import org.jetbrains.annotations.NotNull;

public class HasRecentlyReceivedCancerTherapyOfName implements PassOrFailEvaluator {

    @NotNull
    private final Set<String> namesToFind;
    @NotNull
    private final LocalDate minDate;

    HasRecentlyReceivedCancerTherapyOfName(@NotNull final Set<String> namesToFind, @NotNull final LocalDate minDate) {
        this.namesToFind = namesToFind;
        this.minDate = minDate;
    }

    @Override
    public boolean isPass(@NotNull PatientRecord record) {
        for (Medication medication : record.clinical().medications()) {
            for (String nameToFind : namesToFind) {
                boolean nameIsMatch = medication.name().equalsIgnoreCase(nameToFind);
                boolean dateIsMatch = MedicationDateEvaluation.hasBeenGivenAfterDate(medication, minDate);

                if (nameIsMatch && dateIsMatch) {
                    return true;
                }
            }
        }
        return false;
    }

    @NotNull
    @Override
    public String passMessage() {
        return "Patient has recently received anti-cancer medication " + concat(namesToFind);
    }

    @NotNull
    @Override
    public String failMessage() {
        return "Patient has not recently received anti-cancer medication " + concat(namesToFind);
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
