package com.hartwig.actin.algo.evaluation.medication;

import java.util.Set;

import com.hartwig.actin.PatientRecord;
import com.hartwig.actin.algo.datamodel.Evaluation;
import com.hartwig.actin.algo.datamodel.EvaluationResult;
import com.hartwig.actin.algo.datamodel.ImmutableEvaluation;
import com.hartwig.actin.algo.evaluation.EvaluationFunction;
import com.hartwig.actin.algo.evaluation.util.Format;

import org.jetbrains.annotations.NotNull;

public class CurrentlyGetsMedicationOfName implements EvaluationFunction {

    @NotNull
    private final Set<String> termsToFind;

    CurrentlyGetsMedicationOfName(@NotNull final Set<String> termsToFind) {
        this.termsToFind = termsToFind;
    }

    @NotNull
    @Override
    public Evaluation evaluate(@NotNull PatientRecord record) {
        boolean hasReceivedMedication = !MedicationFilter.withAnyTermInName(record.clinical().medications(), termsToFind).isEmpty();

        EvaluationResult result = hasReceivedMedication ? EvaluationResult.PASS : EvaluationResult.FAIL;
        ImmutableEvaluation.Builder builder = ImmutableEvaluation.builder().result(result);
        if (result == EvaluationResult.FAIL) {
            builder.addFailSpecificMessages("Patient currently does not get medication with name " + Format.concat(termsToFind));
        } else if (result.isPass()) {
            builder.addPassSpecificMessages("Patient currently gets medication with name " + Format.concat(termsToFind));
        }

        return builder.build();
    }
}
