package com.hartwig.actin.algo.evaluation.composite;

import com.hartwig.actin.PatientRecord;
import com.hartwig.actin.algo.datamodel.EvaluationResult;
import com.hartwig.actin.algo.evaluation.EvaluationFunction;

import org.jetbrains.annotations.NotNull;

public class Fallback implements EvaluationFunction {

    @NotNull
    private final EvaluationFunction primary;
    @NotNull
    private final EvaluationFunction secondary;

    public Fallback(@NotNull final EvaluationFunction primary, @NotNull final EvaluationFunction secondary) {
        this.primary = primary;
        this.secondary = secondary;
    }

    @NotNull
    @Override
    public EvaluationResult evaluate(@NotNull PatientRecord record) {
        EvaluationResult primaryEvaluation = primary.evaluate(record);
        return primaryEvaluation != EvaluationResult.UNDETERMINED ? primaryEvaluation : secondary.evaluate(record);
    }
}
