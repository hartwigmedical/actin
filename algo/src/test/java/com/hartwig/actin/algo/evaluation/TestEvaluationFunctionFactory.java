package com.hartwig.actin.algo.evaluation;

import com.hartwig.actin.algo.datamodel.Evaluation;

import org.jetbrains.annotations.NotNull;

public final class TestEvaluationFunctionFactory {

    private TestEvaluationFunctionFactory() {
    }

    @NotNull
    public static EvaluationFunction pass() {
        return fixedEvaluation(Evaluation.PASS);
    }

    @NotNull
    public static EvaluationFunction passButWarn() {
        return fixedEvaluation(Evaluation.PASS_BUT_WARN);
    }

    @NotNull
    public static EvaluationFunction fail() {
        return fixedEvaluation(Evaluation.FAIL);
    }

    @NotNull
    public static EvaluationFunction couldNotBeDetermined() {
        return fixedEvaluation(Evaluation.COULD_NOT_BE_DETERMINED);
    }

    @NotNull
    private static EvaluationFunction fixedEvaluation(@NotNull Evaluation output) {
        return record -> output;
    }
}