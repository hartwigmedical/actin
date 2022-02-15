package com.hartwig.actin.algo.evaluation.composite;

import com.hartwig.actin.PatientRecord;
import com.hartwig.actin.algo.datamodel.Evaluation;
import com.hartwig.actin.algo.datamodel.EvaluationResult;
import com.hartwig.actin.algo.datamodel.ImmutableEvaluation;
import com.hartwig.actin.algo.evaluation.EvaluationFunction;

import org.jetbrains.annotations.NotNull;

public class Not implements EvaluationFunction {

    @NotNull
    private final EvaluationFunction function;

    public Not(@NotNull final EvaluationFunction function) {
        this.function = function;
    }

    @NotNull
    @Override
    public Evaluation evaluate(@NotNull PatientRecord record) {
        Evaluation evaluation = function.evaluate(record);

        EvaluationResult negatedResult = null;
        if (evaluation.result() == EvaluationResult.PASS || evaluation.result() == EvaluationResult.PASS_BUT_WARN) {
            negatedResult = EvaluationResult.FAIL;
        } else if (evaluation.result() == EvaluationResult.FAIL) {
            negatedResult = EvaluationResult.PASS;
        } else if (evaluation.result() == EvaluationResult.UNDETERMINED || evaluation.result() == EvaluationResult.NOT_IMPLEMENTED
                || evaluation.result() == EvaluationResult.NOT_EVALUATED) {
            negatedResult = evaluation.result();
        }

        if (negatedResult == null) {
            throw new IllegalStateException("NOT function cannot negate evaluation: " + evaluation);
        }

        return ImmutableEvaluation.builder().result(negatedResult).messages(evaluation.messages()).build();
    }
}
