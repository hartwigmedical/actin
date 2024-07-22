package com.hartwig.actin.algo.datamodel

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
