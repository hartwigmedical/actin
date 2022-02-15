package com.hartwig.actin.algo.evaluation.composite;

import com.hartwig.actin.PatientRecord;
import com.hartwig.actin.algo.datamodel.Evaluation;
import com.hartwig.actin.algo.datamodel.EvaluationResult;
import com.hartwig.actin.algo.datamodel.ImmutableEvaluation;
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

        EvaluationResult updatedResult;
        switch (evaluation.result()) {
            case PASS:
            case PASS_BUT_WARN:
                updatedResult = EvaluationResult.PASS_BUT_WARN;
                break;
            case FAIL:
                updatedResult = EvaluationResult.PASS;
                break;
            case NOT_IMPLEMENTED:
            case UNDETERMINED:
            case NOT_EVALUATED:
                updatedResult = evaluation.result();
                break;
            default: {
                throw new IllegalStateException("Could not determine output for WarnOnPass for evaluation result: " + evaluation.result());
            }
        }

        return ImmutableEvaluation.builder().result(updatedResult).messages(evaluation.messages()).build();
    }
}
