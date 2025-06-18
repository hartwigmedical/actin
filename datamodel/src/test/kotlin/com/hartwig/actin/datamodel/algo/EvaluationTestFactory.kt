package com.hartwig.actin.datamodel.algo

object EvaluationTestFactory {

    fun withResult(result: EvaluationResult): Evaluation {
        val base = Evaluation(result = result, recoverable = false)

        return when (result) {
            EvaluationResult.PASS -> {
                base.copy(passMessages = setOf(StaticMessage("pass")))
            }

            EvaluationResult.NOT_EVALUATED -> {
                base.copy(passMessages = setOf(StaticMessage("not evaluated")))
            }

            EvaluationResult.WARN -> {
                base.copy(warnMessages = setOf(StaticMessage("warn")))
            }

            EvaluationResult.UNDETERMINED -> {
                base.copy(undeterminedMessages = setOf(StaticMessage("undetermined")))
            }

            EvaluationResult.FAIL -> {
                base.copy(failMessages = setOf(StaticMessage("fail")))
            }
        }
    }
}
