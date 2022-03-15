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

public class CurrentlyGetsStableMedicationOfName implements EvaluationFunction {

    @NotNull
    private final Set<String> termsToFind;

    CurrentlyGetsStableMedicationOfName(@NotNull final Set<String> termsToFind) {
        this.termsToFind = termsToFind;
    }

    @NotNull
    @Override
    public Evaluation evaluate(@NotNull PatientRecord record) {
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

        EvaluationResult result = hasFoundOnePassingTerm ? EvaluationResult.PASS : EvaluationResult.FAIL;
        ImmutableEvaluation.Builder builder = ImmutableEvaluation.builder().result(result);
        if (result == EvaluationResult.FAIL) {
            builder.addFailSpecificMessages("Patient does not get stable dosing of medication with name " + Format.concat(termsToFind));
        } else if (result.isPass()) {
            builder.addPassSpecificMessages("Patient gets stable dosing of medication with name " + Format.concat(termsToFind));
        }

        return builder.build();
    }
}
