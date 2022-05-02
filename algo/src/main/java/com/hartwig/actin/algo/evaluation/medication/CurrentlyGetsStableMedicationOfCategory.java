package com.hartwig.actin.algo.evaluation.medication;

import java.util.List;
import java.util.Set;

import com.hartwig.actin.PatientRecord;
import com.hartwig.actin.algo.datamodel.Evaluation;
import com.hartwig.actin.algo.datamodel.EvaluationResult;
import com.hartwig.actin.algo.datamodel.ImmutableEvaluation;
import com.hartwig.actin.algo.evaluation.EvaluationFactory;
import com.hartwig.actin.algo.evaluation.EvaluationFunction;
import com.hartwig.actin.algo.evaluation.util.Format;
import com.hartwig.actin.clinical.datamodel.Medication;

import org.jetbrains.annotations.NotNull;

public class CurrentlyGetsStableMedicationOfCategory implements EvaluationFunction {

    @NotNull
    private final MedicationSelector selector;
    @NotNull
    private final Set<String> categoriesToFind;

    CurrentlyGetsStableMedicationOfCategory(@NotNull final MedicationSelector selector, @NotNull final Set<String> categoriesToFind) {
        this.selector = selector;
        this.categoriesToFind = categoriesToFind;
    }

    @NotNull
    @Override
    public Evaluation evaluate(@NotNull PatientRecord record) {
        boolean hasFoundOnePassingCategory = false;
        for (String categoryToFind : categoriesToFind) {
            boolean hasActiveAndStableMedication = false;
            Medication referenceDosing = null;
            List<Medication> filtered = selector.withExactCategory(record.clinical().medications(), categoryToFind);
            for (Medication medication : filtered) {
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
                hasFoundOnePassingCategory = true;
            }
        }

        EvaluationResult result = hasFoundOnePassingCategory ? EvaluationResult.PASS : EvaluationResult.FAIL;
        ImmutableEvaluation.Builder builder = EvaluationFactory.unrecoverable().result(result);
        if (result == EvaluationResult.FAIL) {
            builder.addFailSpecificMessages(
                    "Patient does not get stable dosing of medication with category " + Format.concat(categoriesToFind));
            builder.addFailGeneralMessages("No stable dosing of " + Format.concat(categoriesToFind));
        } else if (result == EvaluationResult.PASS) {
            builder.addPassSpecificMessages("Patient gets stable dosing of medication with category " + Format.concat(categoriesToFind));
            builder.addPassGeneralMessages("Stable dosing of " + Format.concat(categoriesToFind));
        }

        return builder.build();
    }
}
