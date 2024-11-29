package com.hartwig.actin.datamodel.algo

object EvaluationTestFactory {

    fun withResult(result: EvaluationResult, recoverable: Boolean = false): Evaluation {
        val base = Evaluation(result = result, recoverable = recoverable)

        return when (result) {
            EvaluationResult.PASS -> {
                base.copy(passSpecificMessages = setOf("pass specific"), passGeneralMessages = setOf("pass general"))
            }

            EvaluationResult.NOT_EVALUATED -> {
                base.copy(passSpecificMessages = setOf("not evaluated specific"), passGeneralMessages = setOf("not evaluated general"))
            }

            EvaluationResult.WARN -> {
                base.copy(warnSpecificMessages = setOf("warn specific"), warnGeneralMessages = setOf("warn general"))
            }

            EvaluationResult.UNDETERMINED -> {
                base.copy(
                    undeterminedSpecificMessages = setOf("undetermined specific"),
                    undeterminedGeneralMessages = setOf("undetermined general")
                )
            }

            EvaluationResult.FAIL -> {
                base.copy(failSpecificMessages = setOf("fail specific"), failGeneralMessages = setOf("fail general"))
            }
        }
    }
}
