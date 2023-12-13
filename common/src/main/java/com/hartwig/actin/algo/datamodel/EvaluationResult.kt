package com.hartwig.actin.algo.datamodel

enum class EvaluationResult {
    PASS,
    WARN,
    FAIL,
    UNDETERMINED,
    NOT_EVALUATED,
    NOT_IMPLEMENTED;

    fun isWorseThan(otherResult: EvaluationResult): Boolean {
        return when (otherResult) {
            NOT_IMPLEMENTED -> {
                false
            }

            FAIL -> {
                this == NOT_IMPLEMENTED
            }

            WARN -> {
                this == NOT_IMPLEMENTED || this == FAIL
            }

            UNDETERMINED -> {
                this == NOT_IMPLEMENTED || this == FAIL || this == WARN
            }

            PASS -> {
                this == NOT_IMPLEMENTED || this == FAIL || this == WARN || this == UNDETERMINED
            }

            NOT_EVALUATED -> {
                this == NOT_IMPLEMENTED || this == FAIL || this == WARN || this == UNDETERMINED || this == PASS
            }

            else -> {
                throw IllegalStateException("Cannot compare evaluation result with $otherResult")
            }
        }
    }
}
