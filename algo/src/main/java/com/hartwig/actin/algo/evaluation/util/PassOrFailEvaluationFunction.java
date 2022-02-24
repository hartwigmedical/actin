package com.hartwig.actin.algo.evaluation.util;

import com.hartwig.actin.PatientRecord;
import com.hartwig.actin.algo.datamodel.Evaluation;
import com.hartwig.actin.algo.datamodel.EvaluationResult;
import com.hartwig.actin.algo.datamodel.ImmutableEvaluation;
import com.hartwig.actin.algo.evaluation.EvaluationFunction;

import org.jetbrains.annotations.NotNull;

public class PassOrFailEvaluationFunction implements EvaluationFunction {

    @NotNull
    private final PassOrFailEvaluator evaluator;

    public PassOrFailEvaluationFunction(@NotNull final PassOrFailEvaluator evaluator) {
        this.evaluator = evaluator;
    }

    @NotNull
    @Override
    public Evaluation evaluate(@NotNull PatientRecord record) {
        boolean isPass = evaluator.isPass(record);

        EvaluationResult result = isPass ? EvaluationResult.PASS : EvaluationResult.FAIL;
        ImmutableEvaluation.Builder builder = ImmutableEvaluation.builder().result(result);
        if (result == EvaluationResult.FAIL) {
            builder.addFailMessages(evaluator.failMessage());
        } else if (result == EvaluationResult.PASS) {
            builder.addPassMessages(evaluator.passMessage());
        }

        return builder.build();
    }
}
