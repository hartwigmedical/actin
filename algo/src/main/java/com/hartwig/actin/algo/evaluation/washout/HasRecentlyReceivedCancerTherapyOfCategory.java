package com.hartwig.actin.algo.evaluation.washout;

import java.time.LocalDate;
import java.util.Set;
import java.util.StringJoiner;

import com.hartwig.actin.PatientRecord;
import com.hartwig.actin.algo.evaluation.util.PassOrFailEvaluator;
import com.hartwig.actin.clinical.datamodel.Medication;

import org.jetbrains.annotations.NotNull;

public class HasRecentlyReceivedCancerTherapyOfCategory implements PassOrFailEvaluator {

    @NotNull
    private final Set<String> categoriesToFind;
    @NotNull
    private final LocalDate minDate;

    HasRecentlyReceivedCancerTherapyOfCategory(@NotNull final Set<String> categoriesToFind, @NotNull final LocalDate minDate) {
        this.categoriesToFind = categoriesToFind;
        this.minDate = minDate;
    }

    @Override
    public boolean isPass(@NotNull PatientRecord record) {
        for (Medication medication : record.clinical().medications()) {
            for (String categoryToFind : categoriesToFind) {
                for (String category : medication.categories()) {
                    boolean categoryIsMatch = category.toLowerCase().contains(categoryToFind.toLowerCase());
                    boolean dateIsMatch = MedicationDateEvaluation.hasBeenGivenAfterDate(medication, minDate);

                    if (categoryIsMatch && dateIsMatch) {
                        return true;
                    }
                }
            }
        } return false;
    }

    @NotNull
    @Override
    public String passMessage() {
        return "Patient has recently received medication of category " + concat(categoriesToFind);
    }

    @NotNull
    @Override
    public String failMessage() {
        return "Patient has not recently received medication of category " + concat(categoriesToFind);
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
