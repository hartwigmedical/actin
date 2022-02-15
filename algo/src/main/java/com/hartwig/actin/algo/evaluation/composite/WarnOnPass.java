package com.hartwig.actin.algo.evaluation.composite;

import com.hartwig.actin.PatientRecord;
import com.hartwig.actin.algo.datamodel.EvaluationResult;
import com.hartwig.actin.algo.evaluation.EvaluationFunction;

import org.jetbrains.annotations.NotNull;

public class WarnOnPass implements EvaluationFunction {

    @NotNull
    private final EvaluationFunction function;

    public WarnOnPass(@NotNull final EvaluationFunction function) {
        this.function = function;
    }

    @NotNull
    @Override
    public EvaluationResult evaluate(@NotNull PatientRecord record) {
        EvaluationResult evaluation = function.evaluate(record);

        switch (evaluation) {
            case PASS:
            case PASS_BUT_WARN:
                return EvaluationResult.PASS_BUT_WARN;
            case FAIL:
                return EvaluationResult.PASS;
            case NOT_IMPLEMENTED:
            case UNDETERMINED:
            case NOT_EVALUATED:
                return evaluation;
            default: {
                throw new IllegalStateException("Could not determine output for WarnOnPass for evaluation: " + evaluation);
            }
        }
    }
}
