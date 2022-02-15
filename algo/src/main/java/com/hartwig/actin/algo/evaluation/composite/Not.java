package com.hartwig.actin.algo.evaluation.composite;

import com.hartwig.actin.PatientRecord;
import com.hartwig.actin.algo.datamodel.EvaluationResult;
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
    public EvaluationResult evaluate(@NotNull PatientRecord record) {
        EvaluationResult evaluation = function.evaluate(record);

        if (evaluation == EvaluationResult.PASS || evaluation == EvaluationResult.PASS_BUT_WARN) {
            return EvaluationResult.FAIL;
        } else if (evaluation == EvaluationResult.FAIL) {
            return EvaluationResult.PASS;
        } else if (evaluation == EvaluationResult.UNDETERMINED || evaluation == EvaluationResult.NOT_IMPLEMENTED
                || evaluation == EvaluationResult.NOT_EVALUATED) {
            return evaluation;
        }

        throw new IllegalStateException("NOT function cannot negate evaluation: " + evaluation);
    }
}
