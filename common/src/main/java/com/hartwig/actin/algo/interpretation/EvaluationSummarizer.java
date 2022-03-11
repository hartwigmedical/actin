package com.hartwig.actin.algo.interpretation;

import com.hartwig.actin.algo.datamodel.Evaluation;

import org.jetbrains.annotations.NotNull;

public final class EvaluationSummarizer {

    private EvaluationSummarizer() {
    }

    @NotNull
    public static EvaluationSummary summarize(@NotNull Iterable<Evaluation> evaluations) {
        int count = 0;
        int passedCount = 0;
        int warningCount = 0;
        int failedCount = 0;
        int undeterminedCount = 0;
        int notEvaluatedCount = 0;
        int nonImplementedCount = 0;

        for (Evaluation evaluation : evaluations) {
            count++;
            switch (evaluation.result()) {
                case PASS: {
                    passedCount++;
                    break;
                }
                case WARN: {
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
                case NOT_EVALUATED: {
                    notEvaluatedCount++;
                    break;
                }
                case NOT_IMPLEMENTED: {
                    nonImplementedCount++;
                    break;
                }
                default: {
                    throw new IllegalStateException("Cannot summarize evaluation with result '" + evaluation.result() + "'");
                }
            }
        }

        return ImmutableEvaluationSummary.builder()
                .count(count)
                .passedCount(passedCount)
                .warningCount(warningCount)
                .failedCount(failedCount)
                .undeterminedCount(undeterminedCount)
                .notEvaluatedCount(notEvaluatedCount)
                .nonImplementedCount(nonImplementedCount)
                .build();
    }

    @NotNull
    public static EvaluationSummary sum(@NotNull Iterable<EvaluationSummary> summaries) {
        int count = 0;
        int passedCount = 0;
        int warningCount = 0;
        int failedCount = 0;
        int undeterminedCount = 0;
        int notEvaluatedCount = 0;
        int nonImplementedCount = 0;

        for (EvaluationSummary summary : summaries) {
            count += summary.count();
            passedCount += summary.passedCount();
            warningCount += summary.warningCount();
            failedCount += summary.failedCount();
            undeterminedCount += summary.undeterminedCount();
            notEvaluatedCount += summary.notEvaluatedCount();
            nonImplementedCount += summary.nonImplementedCount();
        }

        return ImmutableEvaluationSummary.builder()
                .count(count)
                .passedCount(passedCount)
                .warningCount(warningCount)
                .failedCount(failedCount)
                .undeterminedCount(undeterminedCount)
                .notEvaluatedCount(notEvaluatedCount)
                .nonImplementedCount(nonImplementedCount)
                .build();
    }
}
