package com.hartwig.actin.algo.evaluation.composite;

import java.util.Set;

import com.google.common.collect.Sets;
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
        Set<String> passMessages = evaluation.passMessages();
        Set<String> failMessages = evaluation.failMessages();
        switch (evaluation.result()) {
            case PASS:
            case PASS_BUT_WARN:
                updatedResult = EvaluationResult.PASS_BUT_WARN;
                break;
            case FAIL:
                updatedResult = EvaluationResult.PASS;
                passMessages = evaluation.failMessages();
                failMessages = Sets.newHashSet();
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

        return ImmutableEvaluation.builder()
                .result(updatedResult)
                .passMessages(passMessages)
                .undeterminedMessages(evaluation.undeterminedMessages())
                .failMessages(failMessages)
                .build();
    }
}
