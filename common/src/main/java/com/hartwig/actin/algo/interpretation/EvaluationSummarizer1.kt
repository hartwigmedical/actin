package com.hartwig.actin.algo.interpretation

import com.hartwig.actin.algo.datamodel.Evaluation
import com.hartwig.actin.algo.datamodel.EvaluationResult

object EvaluationSummarizer {
    fun summarize(evaluations: Iterable<Evaluation>): EvaluationSummary {
        var count = 0
        var passedCount = 0
        var warningCount = 0
        var failedCount = 0
        var undeterminedCount = 0
        var notEvaluatedCount = 0
        var nonImplementedCount = 0
        for (evaluation in evaluations) {
            count++
            when (evaluation.result()) {
                EvaluationResult.PASS -> {
                    passedCount++
                }

                EvaluationResult.WARN -> {
                    warningCount++
                }

                EvaluationResult.FAIL -> {
                    failedCount++
                }

                EvaluationResult.UNDETERMINED -> {
                    undeterminedCount++
                }

                EvaluationResult.NOT_EVALUATED -> {
                    notEvaluatedCount++
                }

                EvaluationResult.NOT_IMPLEMENTED -> {
                    nonImplementedCount++
                }

                else -> {
                    throw IllegalStateException("Cannot summarize evaluation with result '" + evaluation.result() + "'")
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
            .build()
    }

    fun sum(summaries: Iterable<EvaluationSummary>): EvaluationSummary {
        var count = 0
        var passedCount = 0
        var warningCount = 0
        var failedCount = 0
        var undeterminedCount = 0
        var notEvaluatedCount = 0
        var nonImplementedCount = 0
        for (summary in summaries) {
            count += summary.count()
            passedCount += summary.passedCount()
            warningCount += summary.warningCount()
            failedCount += summary.failedCount()
            undeterminedCount += summary.undeterminedCount()
            notEvaluatedCount += summary.notEvaluatedCount()
            nonImplementedCount += summary.nonImplementedCount()
        }
        return ImmutableEvaluationSummary.builder()
            .count(count)
            .passedCount(passedCount)
            .warningCount(warningCount)
            .failedCount(failedCount)
            .undeterminedCount(undeterminedCount)
            .notEvaluatedCount(notEvaluatedCount)
            .nonImplementedCount(nonImplementedCount)
            .build()
    }
}
