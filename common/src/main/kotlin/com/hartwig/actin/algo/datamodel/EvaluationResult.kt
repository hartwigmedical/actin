package com.hartwig.actin.algo.datamodel

enum class EvaluationResult {
    NOT_IMPLEMENTED,
    FAIL,
    WARN,
    UNDETERMINED,
    PASS,
    NOT_EVALUATED;

    fun isWorseThan(otherResult: EvaluationResult): Boolean {
        return this < otherResult
    }
}
