package com.hartwig.actin.algo.evaluation.medication;

import java.util.Set;

import com.hartwig.actin.PatientRecord;
import com.hartwig.actin.algo.datamodel.Evaluation;
import com.hartwig.actin.algo.datamodel.EvaluationResult;
import com.hartwig.actin.algo.datamodel.ImmutableEvaluation;
import com.hartwig.actin.algo.evaluation.EvaluationFunction;
import com.hartwig.actin.algo.evaluation.util.Format;
import com.hartwig.actin.clinical.datamodel.Medication;

import org.jetbrains.annotations.NotNull;

public class CurrentlyGetsStableMedicationOfCategory implements EvaluationFunction {

    @NotNull
    private final Set<String> categoriesToFind;

    CurrentlyGetsStableMedicationOfCategory(@NotNull final Set<String> categoriesToFind) {
        this.categoriesToFind = categoriesToFind;
    }

    @NotNull
    @Override
    public Evaluation evaluate(@NotNull PatientRecord record) {
        boolean hasFoundOnePassingCategory = false;
        for (String categoryToFind : categoriesToFind) {
            boolean hasActiveAndStableMedication = false;
            Medication referenceDosing = null;
            for (Medication medication : MedicationFilter.withExactCategory(record.clinical().medications(), categoryToFind)) {
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
        ImmutableEvaluation.Builder builder = ImmutableEvaluation.builder().result(result);
        if (result == EvaluationResult.FAIL) {
            builder.addFailMessages("Patient does not get stable dosing of medication with category " + Format.concat(categoriesToFind));
        } else if (result.isPass()) {
            builder.addPassMessages("Patient gets stable dosing of medication with category " + Format.concat(categoriesToFind));
        }

        return builder.build();
    }
}
