package com.hartwig.actin.algo.evaluation.composite;

import java.util.Set;

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

        EvaluationResult negatedResult;
        Set<String> passSpecificMessages;
        Set<String> passGeneralMessages;
        Set<String> failSpecificMessages;
        Set<String> failGeneralMessages;
        if (evaluation.result() == EvaluationResult.PASS) {
            negatedResult = EvaluationResult.FAIL;
            passSpecificMessages = evaluation.failSpecificMessages();
            passGeneralMessages = evaluation.failGeneralMessages();
            failSpecificMessages = evaluation.passSpecificMessages();
            failGeneralMessages = evaluation.passGeneralMessages();
        } else if (evaluation.result() == EvaluationResult.FAIL) {
            negatedResult = EvaluationResult.PASS;
            passSpecificMessages = evaluation.failSpecificMessages();
            passGeneralMessages = evaluation.failGeneralMessages();
            failSpecificMessages = evaluation.passSpecificMessages();
            failGeneralMessages = evaluation.passGeneralMessages();
        } else {
            negatedResult = evaluation.result();
            passSpecificMessages = evaluation.passSpecificMessages();
            passGeneralMessages = evaluation.passGeneralMessages();
            failSpecificMessages = evaluation.failSpecificMessages();
            failGeneralMessages = evaluation.failGeneralMessages();
        }

        return ImmutableEvaluation.builder()
                .result(negatedResult)
                .passSpecificMessages(passSpecificMessages)
                .passGeneralMessages(passGeneralMessages)
                .warnSpecificMessages(evaluation.warnSpecificMessages())
                .warnGeneralMessages(evaluation.warnGeneralMessages())
                .undeterminedSpecificMessages(evaluation.undeterminedSpecificMessages())
                .undeterminedGeneralMessages(evaluation.undeterminedGeneralMessages())
                .failSpecificMessages(failSpecificMessages)
                .failGeneralMessages(failGeneralMessages)
                .build();
    }
}
