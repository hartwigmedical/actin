package com.hartwig.actin.datamodel.algo

enum class EvaluationResult {
    FAIL,
    WARN,
    UNDETERMINED,
    PASS,
    NOT_EVALUATED;

    fun isWorseThan(otherResult: EvaluationResult): Boolean {
        return this < otherResult
    }
}
