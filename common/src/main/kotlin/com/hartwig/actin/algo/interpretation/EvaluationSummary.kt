package com.hartwig.actin.algo.interpretation

data class EvaluationSummary(
    val count: Int = 0,
    val warningCount: Int = 0,
    val passedCount: Int = 0,
    val failedCount: Int = 0,
    val undeterminedCount: Int = 0,
    val notEvaluatedCount: Int = 0,
) {

    operator fun plus(other: EvaluationSummary) = EvaluationSummary(
        count = count + other.count,
        warningCount = warningCount + other.warningCount,
        passedCount = passedCount + other.passedCount,
        failedCount = failedCount + other.failedCount,
        undeterminedCount = undeterminedCount + other.undeterminedCount,
        notEvaluatedCount = notEvaluatedCount + other.notEvaluatedCount,
    )
}
