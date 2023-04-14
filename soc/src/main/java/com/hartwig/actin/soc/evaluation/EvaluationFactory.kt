package com.hartwig.actin.soc.evaluation

import com.hartwig.actin.algo.datamodel.Evaluation
import com.hartwig.actin.algo.datamodel.EvaluationResult
import com.hartwig.actin.algo.datamodel.ImmutableEvaluation

object EvaluationFactory {
    fun recoverable(): ImmutableEvaluation.Builder {
        return ImmutableEvaluation.builder().recoverable(true)
    }

    fun unrecoverable(): ImmutableEvaluation.Builder {
        return ImmutableEvaluation.builder().recoverable(false)
    }

    fun pass(specificMessage: String, generalMessage: String): Evaluation {
        return unrecoverable().result(EvaluationResult.PASS)
                .addPassSpecificMessages(specificMessage)
                .addPassGeneralMessages(generalMessage)
                .build()
    }

    fun fail(specificMessage: String, generalMessage: String): Evaluation {
        return buildFailEvaluation(unrecoverable(), specificMessage, generalMessage)
    }

    fun recoverableFail(specificMessage: String, generalMessage: String): Evaluation {
        return buildFailEvaluation(recoverable(), specificMessage, generalMessage)
    }

    fun undetermined(specificMessage: String, generalMessage: String): Evaluation {
        return unrecoverable().result(EvaluationResult.UNDETERMINED)
                .addUndeterminedSpecificMessages(specificMessage)
                .addUndeterminedGeneralMessages(generalMessage)
                .build()
    }

    fun warn(specificMessage: String, generalMessage: String): Evaluation {
        return unrecoverable().result(EvaluationResult.WARN)
                .addWarnSpecificMessages(specificMessage)
                .addWarnGeneralMessages(generalMessage)
                .build()
    }

    fun notEvaluated(specificMessage: String, generalMessage: String): Evaluation {
        return unrecoverable().result(EvaluationResult.NOT_EVALUATED)
                .addWarnSpecificMessages(specificMessage)
                .addWarnGeneralMessages(generalMessage)
                .build()
    }

    private fun buildFailEvaluation(builder: ImmutableEvaluation.Builder, specificMessage: String, generalMessage: String): Evaluation {
        return builder.result(EvaluationResult.FAIL)
                .addFailSpecificMessages(specificMessage)
                .addFailGeneralMessages(generalMessage)
                .build()
    }
}