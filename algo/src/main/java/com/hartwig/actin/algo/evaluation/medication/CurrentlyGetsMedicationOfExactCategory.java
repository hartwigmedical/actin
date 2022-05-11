package com.hartwig.actin.algo.evaluation.medication;

import java.util.List;
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

public class CurrentlyGetsMedicationOfExactCategory implements EvaluationFunction {

    @NotNull
    private final MedicationSelector selector;
    @NotNull
    private final Set<String> categoriesToFind;

    CurrentlyGetsMedicationOfExactCategory(@NotNull final MedicationSelector selector, @NotNull final Set<String> categoriesToFind) {
        this.selector = selector;
        this.categoriesToFind = categoriesToFind;
    }

    @NotNull
    @Override
    public Evaluation evaluate(@NotNull PatientRecord record) {
        List<Medication> medications = selector.activeWithAnyExactCategory(record.clinical().medications(), categoriesToFind);

        if (!medications.isEmpty()) {
            Set<String> names = Sets.newHashSet();
            for (Medication medication : medications) {
                names.add(medication.name());
            }

            return EvaluationFactory.unrecoverable()
                    .result(EvaluationResult.PASS)
                    .addPassSpecificMessages(
                            "Patient currently gets medication " + Format.concat(names) + ", which belong(s) to category " + Format.concat(
                                    categoriesToFind))
                    .addPassGeneralMessages(Format.concat(categoriesToFind) + " medication")
                    .build();
        }

        return EvaluationFactory.unrecoverable()
                .result(EvaluationResult.FAIL)
                .addFailSpecificMessages("Patient currently does not get medication of category " + Format.concat(categoriesToFind))
                .addFailGeneralMessages("No " + Format.concat(categoriesToFind) + " medication")
                .build();
    }
}
