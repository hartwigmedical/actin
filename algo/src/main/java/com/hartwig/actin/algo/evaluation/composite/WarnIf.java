package com.hartwig.actin.algo.evaluation.composite;

import com.hartwig.actin.PatientRecord;
import com.hartwig.actin.algo.datamodel.Evaluation;
import com.hartwig.actin.algo.datamodel.EvaluationResult;
import com.hartwig.actin.algo.datamodel.ImmutableEvaluation;
import com.hartwig.actin.algo.evaluation.EvaluationFunction;

import org.jetbrains.annotations.NotNull;

public class WarnIf implements EvaluationFunction {

    @NotNull
    private final EvaluationFunction function;

    public WarnIf(@NotNull final EvaluationFunction function) {
        this.function = function;
    }

    @NotNull
    @Override
    public Evaluation evaluate(@NotNull PatientRecord record) {
        Evaluation evaluation = function.evaluate(record);

        if (evaluation.result() == EvaluationResult.PASS) {
            return ImmutableEvaluation.builder()
                    .result(EvaluationResult.WARN)
                    .warnSpecificMessages(evaluation.passSpecificMessages())
                    .warnGeneralMessages(evaluation.passGeneralMessages())
                    .build();
        } else if (evaluation.result() == EvaluationResult.WARN) {
            return evaluation;
        }

        ImmutableEvaluation.Builder builder = ImmutableEvaluation.builder()
                .result(EvaluationResult.PASS)
                .addAllPassSpecificMessages(evaluation.passSpecificMessages())
                .addAllPassSpecificMessages(evaluation.warnSpecificMessages())
                .addAllPassSpecificMessages(evaluation.undeterminedSpecificMessages())
                .addAllPassSpecificMessages(evaluation.failSpecificMessages())
                .addAllPassGeneralMessages(evaluation.passGeneralMessages())
                .addAllPassGeneralMessages(evaluation.warnGeneralMessages())
                .addAllPassGeneralMessages(evaluation.undeterminedGeneralMessages())
                .addAllPassGeneralMessages(evaluation.failGeneralMessages());

        if (evaluation.result() == EvaluationResult.NOT_IMPLEMENTED) {
            builder.addPassGeneralMessages("not implemented");
            builder.addPassSpecificMessages("not implemented");
        }

        return builder.build();
    }
}
