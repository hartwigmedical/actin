package com.hartwig.actin.algo.evaluation.composite;

import com.hartwig.actin.PatientRecord;
import com.hartwig.actin.algo.datamodel.Evaluation;
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
    public Evaluation evaluate(@NotNull PatientRecord record) {
        Evaluation evaluation = function.evaluate(record);

        switch (evaluation) {
            case PASS:
            case PASS_BUT_WARN:
                return Evaluation.PASS_BUT_WARN;
            case FAIL:
                return Evaluation.PASS;
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
