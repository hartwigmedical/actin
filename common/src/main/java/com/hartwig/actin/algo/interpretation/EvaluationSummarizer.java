package com.hartwig.actin.algo.interpretation;

import com.hartwig.actin.algo.datamodel.Evaluation;

import org.jetbrains.annotations.NotNull;

public final class EvaluationSummarizer {

    private EvaluationSummarizer() {
    }

    @NotNull
    public static EvaluationSummary summarize(@NotNull Iterable<Evaluation> evaluations) {
        int count = 0;
        int passCount = 0;
        int warningCount = 0;
        int failedCount = 0;
        int undeterminedCount = 0;
        int nonImplementedCount = 0;

        for (Evaluation evaluation : evaluations) {
            count++;
            switch (evaluation) {
                case PASS: {
                    passCount++;
                    break;
                }
                case PASS_BUT_WARN: {
                    passCount++;
                    warningCount++;
                    break;
                }
                case FAIL: {
                    failedCount++;
                    break;
                }
                case UNDETERMINED: {
                    undeterminedCount++;
                    break;
                }
                case NOT_IMPLEMENTED: {
                    nonImplementedCount++;
                    break;
                }
                default: {
                    throw new IllegalStateException("Cannot summarize evaluation of type '" + evaluation + "'");
                }
            }
        }

        return ImmutableEvaluationSummary.builder()
                .count(count)
                .passedCount(passCount)
                .warningCount(warningCount)
                .failedCount(failedCount)
                .undeterminedCount(undeterminedCount)
                .nonImplementedCount(nonImplementedCount)
                .build();
    }
}
