package com.hartwig.actin.algo.evaluation.washout;

import java.time.LocalDate;
import java.util.Set;
import java.util.StringJoiner;

import com.hartwig.actin.PatientRecord;
import com.hartwig.actin.algo.datamodel.Evaluation;
import com.hartwig.actin.algo.datamodel.EvaluationResult;
import com.hartwig.actin.algo.datamodel.ImmutableEvaluation;
import com.hartwig.actin.algo.evaluation.EvaluationFunction;
import com.hartwig.actin.clinical.datamodel.Medication;

import org.jetbrains.annotations.NotNull;

public class HasRecentlyReceivedCancerTherapyOfCategory implements EvaluationFunction {

    @NotNull
    private final Set<String> categoriesToFind;
    @NotNull
    private final LocalDate minDate;

    HasRecentlyReceivedCancerTherapyOfCategory(@NotNull final Set<String> categoriesToFind, @NotNull final LocalDate minDate) {
        this.categoriesToFind = categoriesToFind;
        this.minDate = minDate;
    }

    @NotNull
    @Override
    public Evaluation evaluate(@NotNull PatientRecord record) {
        boolean hasTreatmentOfCategory = false;
        String categoryFound = null;
        for (Medication medication : record.clinical().medications()) {
            for (String categoryToFind : categoriesToFind) {
                for (String category : medication.categories()) {
                    boolean categoryIsMatch = category.toLowerCase().contains(categoryToFind.toLowerCase());
                    boolean dateIsMatch = MedicationDateEvaluation.hasBeenGivenAfterDate(medication, minDate);

                    if (categoryIsMatch && dateIsMatch) {
                        hasTreatmentOfCategory = true;
                        categoryFound = categoryToFind;
                    }
                }
            }
        }

        EvaluationResult result = hasTreatmentOfCategory ? EvaluationResult.PASS : EvaluationResult.FAIL;

        ImmutableEvaluation.Builder builder = ImmutableEvaluation.builder().result(result);
        if (result == EvaluationResult.FAIL) {
            builder.addFailMessages("Patient has not received recent treatments of category " + concat(categoriesToFind));
        } else if (result.isPass()) {
            builder.addPassMessages("Patient has recently received treatment with medication " + categoryFound);
        }

        return builder.build();
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
