package com.hartwig.actin.algo.interpretation

import com.hartwig.actin.algo.datamodel.Evaluation
import com.hartwig.actin.algo.datamodel.EvaluationResult

object EvaluationSummarizer {
   
    fun summarize(evaluations: Iterable<Evaluation>): EvaluationSummary {
        return evaluations.map { evaluation ->
            when (evaluation.result) {
                EvaluationResult.PASS -> {
                    EvaluationSummary(count = 1, passedCount = 1)
                }
                EvaluationResult.WARN -> {
                    EvaluationSummary(count = 1, warningCount = 1)
                }
                EvaluationResult.FAIL -> {
                    EvaluationSummary(count = 1, failedCount = 1)
                }
                EvaluationResult.UNDETERMINED -> {
                    EvaluationSummary(count = 1, undeterminedCount = 1)
                }
                EvaluationResult.NOT_EVALUATED -> {
                    EvaluationSummary(count = 1, notEvaluatedCount = 1)
                }
            }
        }.fold(EvaluationSummary(), EvaluationSummary::plus)
    }
}
