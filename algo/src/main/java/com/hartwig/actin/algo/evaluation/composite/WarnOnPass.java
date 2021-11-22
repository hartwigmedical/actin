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

        if (evaluation == Evaluation.PASS || evaluation == Evaluation.PASS_BUT_WARN) {
            return Evaluation.PASS_BUT_WARN;
        }
        if (evaluation == Evaluation.FAIL) {
            return Evaluation.PASS;
        }
        if (evaluation == Evaluation.UNDETERMINED) {
            return Evaluation.UNDETERMINED;
        }
        else
            return Evaluation.NOT_IMPLEMENTED;
    }
}
