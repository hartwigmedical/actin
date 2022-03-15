package com.hartwig.actin.algo.evaluation.general;

import com.hartwig.actin.PatientRecord;
import com.hartwig.actin.algo.datamodel.Evaluation;
import com.hartwig.actin.algo.datamodel.EvaluationResult;
import com.hartwig.actin.algo.datamodel.ImmutableEvaluation;
import com.hartwig.actin.algo.evaluation.EvaluationFunction;

import org.jetbrains.annotations.NotNull;

public class HasMaximumWHOStatus implements EvaluationFunction {

    private final int maximumWHO;

    HasMaximumWHOStatus(final int maximumWHO) {
        this.maximumWHO = maximumWHO;
    }

    @NotNull
    @Override
    public Evaluation evaluate(@NotNull PatientRecord record) {
        Integer who = record.clinical().clinicalStatus().who();

        if (who == null) {
            return ImmutableEvaluation.builder()
                    .result(EvaluationResult.UNDETERMINED)
                    .addUndeterminedSpecificMessages("WHO status is missing")
                    .build();
        }

        EvaluationResult result = who <= maximumWHO ? EvaluationResult.PASS : EvaluationResult.FAIL;
        ImmutableEvaluation.Builder builder = ImmutableEvaluation.builder().result(result);
        if (result == EvaluationResult.FAIL) {
            builder.addFailSpecificMessages("Patient WHO status " + who + " is worse than requested max (WHO " + maximumWHO + ")");
        } else if (result == EvaluationResult.PASS) {
            builder.addPassSpecificMessages("Patient WHO status " + who + " is within requested max (WHO " + maximumWHO + ")");
        }

        return builder.build();
    }
}
