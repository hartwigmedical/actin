package com.hartwig.actin.algo.evaluation.surgery;

import com.hartwig.actin.PatientRecord;
import com.hartwig.actin.algo.datamodel.Evaluation;
import com.hartwig.actin.algo.datamodel.EvaluationResult;
import com.hartwig.actin.algo.datamodel.ImmutableEvaluation;
import com.hartwig.actin.algo.evaluation.EvaluationFunction;

import org.jetbrains.annotations.NotNull;

public class HasHadSurgery implements EvaluationFunction {

    HasHadSurgery() {
    }

    @NotNull
    @Override
    public Evaluation evaluate(@NotNull PatientRecord record) {
        EvaluationResult result = record.clinical().surgeries().isEmpty() ? EvaluationResult.FAIL : EvaluationResult.PASS;

        ImmutableEvaluation.Builder builder = ImmutableEvaluation.builder().result(result);
        if (result == EvaluationResult.FAIL) {
            builder.addFailMessages("Patient has had no recent surgeries");
        } else if (result == EvaluationResult.PASS) {
            builder.addPassMessages("Patient has had at least one recent surgery");
        }

        return builder.build();
    }
}
