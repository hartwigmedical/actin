package com.hartwig.actin.algo.datamodel

object EvaluationTestFactory {
    fun withResult(result: EvaluationResult): Evaluation {
        val builder = ImmutableEvaluation.builder().recoverable(false).result(result)
        if (result == EvaluationResult.PASS) {
            builder.addPassSpecificMessages("pass specific")
            builder.addPassGeneralMessages("pass general")
        } else if (result == EvaluationResult.NOT_EVALUATED) {
            builder.addPassSpecificMessages("not evaluated specific")
            builder.addPassGeneralMessages("not evaluated general")
        } else if (result == EvaluationResult.WARN) {
            builder.addWarnSpecificMessages("warn specific")
            builder.addWarnGeneralMessages("warn general")
        } else if (result == EvaluationResult.UNDETERMINED) {
            builder.addUndeterminedSpecificMessages("undetermined specific")
            builder.addUndeterminedGeneralMessages("undetermined general")
        } else if (result == EvaluationResult.FAIL) {
            builder.addFailSpecificMessages("fail specific")
            builder.addFailGeneralMessages("fail general")
        }
        return builder.build()
    }
}
