package com.hartwig.actin.algo.evaluation.tumor;

import com.hartwig.actin.PatientRecord;
import com.hartwig.actin.algo.datamodel.Evaluation;
import com.hartwig.actin.algo.datamodel.EvaluationResult;
import com.hartwig.actin.algo.datamodel.ImmutableEvaluation;
import com.hartwig.actin.algo.evaluation.EvaluationFunction;

import org.jetbrains.annotations.NotNull;

public class HasMeasurableDiseaseRecist implements EvaluationFunction {

    HasMeasurableDiseaseRecist() {
    }

    @NotNull
    @Override
    public Evaluation evaluate(@NotNull PatientRecord record) {
        Boolean hasMeasurableDiseaseRecist = record.clinical().tumor().hasMeasurableDisease();
        if (hasMeasurableDiseaseRecist == null) {
            return ImmutableEvaluation.builder()
                    .result(EvaluationResult.UNDETERMINED)
                    .addUndeterminedMessages("Data regarding measurable disease is missing")
                    .build();
        }

        EvaluationResult result = hasMeasurableDiseaseRecist ? EvaluationResult.PASS : EvaluationResult.FAIL;

        ImmutableEvaluation.Builder builder = ImmutableEvaluation.builder().result(result);
        if (result == EvaluationResult.FAIL) {
            builder.addFailMessages("Patient does not have measurable disease according to RECIST");
        } else if (result == EvaluationResult.PASS) {
            builder.addPassMessages("Patient has measurable disease according to RECIST");
        }

        return builder.build();
    }
}
