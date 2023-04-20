package com.hartwig.actin.algo.evaluation.medication;

import java.util.Set;

import com.google.common.collect.Sets;
import com.hartwig.actin.PatientRecord;
import com.hartwig.actin.algo.datamodel.Evaluation;
import com.hartwig.actin.algo.datamodel.EvaluationResult;
import com.hartwig.actin.algo.evaluation.EvaluationFactory;
import com.hartwig.actin.algo.evaluation.EvaluationFunction;
import com.hartwig.actin.algo.evaluation.util.Format;
import com.hartwig.actin.clinical.datamodel.Medication;

import org.jetbrains.annotations.NotNull;

//TODO: Update according to README
public class CurrentlyGetsMedicationOfApproximateCategory implements EvaluationFunction {

    @NotNull
    private final MedicationSelector selector;
    @NotNull
    private final String categoryTermToFind;

    CurrentlyGetsMedicationOfApproximateCategory(@NotNull final MedicationSelector selector,
            @NotNull final String categoryTermToFind) {
        this.selector = selector;
        this.categoryTermToFind = categoryTermToFind;
    }

    @NotNull
    @Override
    public Evaluation evaluate(@NotNull PatientRecord record) {
        Set<String> medications = Sets.newHashSet();
        for (Medication medication : selector.active(record.clinical().medications())) {
            for (String category : medication.categories()) {
                if (category.toLowerCase().contains(categoryTermToFind.toLowerCase())) {
                    medications.add(medication.name());
                }
            }
        }

        if (!medications.isEmpty()) {
            return EvaluationFactory.unrecoverable()
                    .result(EvaluationResult.PASS)
                    .addPassSpecificMessages(
                            "Patient currently gets medication " + Format.concat(medications) + ", which belong(s) to category "
                                    + categoryTermToFind)
                    .addPassGeneralMessages(categoryTermToFind + " medication use")
                    .build();
        }

        return EvaluationFactory.unrecoverable()
                .result(EvaluationResult.FAIL)
                .addFailSpecificMessages("Patient currently does not get medication of category " + categoryTermToFind)
                .addFailGeneralMessages("No " + categoryTermToFind + " medication use")
                .build();
    }
}
