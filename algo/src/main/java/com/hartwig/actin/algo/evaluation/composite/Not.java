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
        Set<String> passMessages;
        Set<String> failMessages;
        if (evaluation.result() == EvaluationResult.PASS || evaluation.result() == EvaluationResult.WARN) {
            negatedResult = EvaluationResult.FAIL;
            passMessages = evaluation.failSpecificMessages();
            failMessages = evaluation.passSpecificMessages();
        } else if (evaluation.result() == EvaluationResult.FAIL) {
            negatedResult = EvaluationResult.PASS;
            passMessages = evaluation.failSpecificMessages();
            failMessages = evaluation.passSpecificMessages();
        } else if (evaluation.result() == EvaluationResult.UNDETERMINED || evaluation.result() == EvaluationResult.NOT_IMPLEMENTED
                || evaluation.result() == EvaluationResult.NOT_EVALUATED) {
            negatedResult = evaluation.result();
            passMessages = evaluation.passSpecificMessages();
            failMessages = evaluation.failSpecificMessages();
        } else {
            throw new IllegalStateException("NOT function cannot negate evaluation: " + evaluation);
        }

        return ImmutableEvaluation.builder()
                .result(negatedResult)
                .passSpecificMessages(passMessages)
                .undeterminedSpecificMessages(evaluation.undeterminedSpecificMessages())
                .failSpecificMessages(failMessages)
                .build();
    }
}
