package com.hartwig.actin.algo.evaluation

import com.hartwig.actin.datamodel.algo.Evaluation
import com.hartwig.actin.datamodel.algo.EvaluationResult

object EvaluationFactory {

    fun pass(message: String, recoverable: Boolean = false, inclusionEvents: Set<String> = emptySet()): Evaluation {
        return Evaluation(
            recoverable = recoverable,
            result = EvaluationResult.PASS,
            passMessages = setOf(message),
            inclusionMolecularEvents = inclusionEvents
        )
    }

    fun recoverablePass(message: String): Evaluation {
        return pass(message, true)
    }

    fun fail(message: String, recoverable: Boolean = false): Evaluation {
        return createFail(recoverable, setOf(message))
    }

    fun recoverableFail(message: String): Evaluation {
        return fail(message, true)
    }

    fun undetermined(
        message: String,
        recoverable: Boolean = false,
        missingGenesForEvaluation: Boolean = false
    ): Evaluation {
        return createUndetermined(recoverable, setOf(message), missingGenesForEvaluation)
    }

    fun recoverableUndetermined(message: String): Evaluation {
        return undetermined(message, true)
    }

    fun warn(
        message: String, recoverable: Boolean = false, inclusionEvents: Set<String> = emptySet()
    ): Evaluation {
        return Evaluation(
            recoverable = recoverable,
            result = EvaluationResult.WARN,
            warnMessages = setOf(message),
            inclusionMolecularEvents = inclusionEvents
        )
    }

    fun recoverableWarn(message: String): Evaluation {
        return warn(message, true)
    }

    fun notEvaluated(message: String): Evaluation {
        return createNotEvaluated(setOf(message))
    }

    private fun createFail(recoverable: Boolean, messages: Set<String>) = Evaluation(
        recoverable = recoverable,
        result = EvaluationResult.FAIL,
        failMessages = messages
    )

    private fun createNotEvaluated(messages: Set<String>) = Evaluation(
        recoverable = false,
        result = EvaluationResult.NOT_EVALUATED,
        passMessages = messages
    )

    private fun createUndetermined(
        recoverable: Boolean,
        messages: Set<String>,
        isMissingGenesForEvaluation: Boolean = false
    ) = Evaluation(
        recoverable = recoverable,
        result = EvaluationResult.UNDETERMINED,
        undeterminedMessages = messages,
        isMissingGenesForSufficientEvaluation = isMissingGenesForEvaluation
    )
}