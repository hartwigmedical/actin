package com.hartwig.actin.algo.evaluation.medication;

import java.util.Set;

import com.google.common.collect.Sets;
import com.hartwig.actin.PatientRecord;
import com.hartwig.actin.algo.datamodel.Evaluation;
import com.hartwig.actin.algo.datamodel.EvaluationResult;
import com.hartwig.actin.algo.datamodel.ImmutableEvaluation;
import com.hartwig.actin.algo.evaluation.EvaluationFunction;
import com.hartwig.actin.algo.evaluation.util.Format;
import com.hartwig.actin.clinical.datamodel.Medication;

import org.jetbrains.annotations.NotNull;

public class CurrentlyGetsMedicationOfApproximateCategory implements EvaluationFunction {

    @NotNull
    private final String categoryTermToFind;

    CurrentlyGetsMedicationOfApproximateCategory(@NotNull final String categoryTermToFind) {
        this.categoryTermToFind = categoryTermToFind;
    }

    @NotNull
    @Override
    public Evaluation evaluate(@NotNull PatientRecord record) {
        Set<String> medications = Sets.newHashSet();
        for (Medication medication : MedicationFilter.active(record.clinical().medications())) {
            for (String category : medication.categories()) {
                if (category.toLowerCase().contains(categoryTermToFind.toLowerCase())) {
                    medications.add(medication.name());
                }
            }
        }

        if (!medications.isEmpty()) {
            return ImmutableEvaluation.builder()
                    .result(EvaluationResult.PASS)
                    .addPassMessages("Patient currently gets medication " + Format.concat(medications) + ", which belong(s) to category "
                            + categoryTermToFind)
                    .build();
        }

        return ImmutableEvaluation.builder()
                .result(EvaluationResult.FAIL)
                .addFailMessages("Patient currently does not get medication of category " + categoryTermToFind)
                .build();
    }
}
