package com.hartwig.actin.algo.evaluation;

import com.hartwig.actin.algo.datamodel.EvaluationResult;
import com.hartwig.actin.algo.datamodel.EvaluationTestFactory;

import org.jetbrains.annotations.NotNull;

public final class TestEvaluationFunctionFactory {

    private TestEvaluationFunctionFactory() {
    }

    @NotNull
    public static EvaluationFunction pass() {
        return fixedEvaluation(EvaluationResult.PASS);
    }

    @NotNull
    public static EvaluationFunction warn() {
        return fixedEvaluation(EvaluationResult.WARN);
    }

    @NotNull
    public static EvaluationFunction fail() {
        return fixedEvaluation(EvaluationResult.FAIL);
    }

    @NotNull
    public static EvaluationFunction undetermined() {
        return fixedEvaluation(EvaluationResult.UNDETERMINED);
    }

    @NotNull
    public static EvaluationFunction notEvaluated() {
        return fixedEvaluation(EvaluationResult.NOT_EVALUATED);
    }

    @NotNull
    public static EvaluationFunction notImplemented() {
        return fixedEvaluation(EvaluationResult.NOT_IMPLEMENTED);
    }

    @NotNull
    private static EvaluationFunction fixedEvaluation(@NotNull EvaluationResult output) {
        return record -> EvaluationTestFactory.withResult(output);
    }
}
