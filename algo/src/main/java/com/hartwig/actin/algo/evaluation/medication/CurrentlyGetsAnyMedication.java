package com.hartwig.actin.algo.evaluation.medication;

import com.hartwig.actin.PatientRecord;
import com.hartwig.actin.algo.datamodel.Evaluation;
import com.hartwig.actin.algo.datamodel.EvaluationResult;
import com.hartwig.actin.algo.datamodel.ImmutableEvaluation;
import com.hartwig.actin.algo.evaluation.EvaluationFunction;

import org.jetbrains.annotations.NotNull;

public class CurrentlyGetsAnyMedication implements EvaluationFunction {

    CurrentlyGetsAnyMedication() {
    }

    @NotNull
    @Override
    public Evaluation evaluate(@NotNull PatientRecord record) {
        boolean hasReceivedMedication = !MedicationFilter.active(record.clinical().medications()).isEmpty();

        EvaluationResult result = hasReceivedMedication ? EvaluationResult.PASS : EvaluationResult.FAIL;
        ImmutableEvaluation.Builder builder = ImmutableEvaluation.builder().result(result);
        if (result == EvaluationResult.FAIL) {
            builder.addFailMessages("Patient does not currently receive medication with status 'active'");
        } else if (result.isPass()) {
            builder.addPassMessages("Patient currently receives medication with status 'active'");
        }

        return builder.build();
    }
}
