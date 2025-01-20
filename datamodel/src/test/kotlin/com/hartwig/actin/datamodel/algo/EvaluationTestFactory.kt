package com.hartwig.actin.datamodel.algo

object EvaluationTestFactory {

    fun withResult(result: EvaluationResult): Evaluation {
        val base = Evaluation(result = result, recoverable = false)

        return when (result) {
            EvaluationResult.PASS -> {
                base.copy(passMessages = setOf("pass"))
            }

            EvaluationResult.NOT_EVALUATED -> {
                base.copy(passMessages = setOf("not evaluated"))
            }

            EvaluationResult.WARN -> {
                base.copy(warnMessages = setOf("warn"))
            }

            EvaluationResult.UNDETERMINED -> {
                base.copy(undeterminedMessages = setOf("undetermined"))
            }

            EvaluationResult.FAIL -> {
                base.copy(failMessages = setOf("fail"))
            }
        }
    }
}
