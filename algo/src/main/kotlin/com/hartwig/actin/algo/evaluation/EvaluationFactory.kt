package com.hartwig.actin.algo.evaluation

import com.hartwig.actin.datamodel.algo.Evaluation
import com.hartwig.actin.datamodel.algo.EvaluationResult
import com.hartwig.actin.datamodel.algo.StaticMessage

object EvaluationFactory {

    fun pass(message: String, recoverable: Boolean = false, inclusionEvents: Set<String> = emptySet()): Evaluation {
        return Evaluation(
            recoverable = recoverable,
            result = EvaluationResult.PASS,
            passMessages = messages(message),
            inclusionMolecularEvents = inclusionEvents
        )
    }

    fun recoverablePass(message: String): Evaluation {
        return pass(message, true)
    }

    fun fail(message: String, recoverable: Boolean = false, isMissingMolecularResultForEvaluation: Boolean = false): Evaluation {
        return Evaluation(
            recoverable = recoverable,
            result = EvaluationResult.FAIL,
            failMessages = messages(message),
            isMissingMolecularResultForEvaluation = isMissingMolecularResultForEvaluation
        )
    }

    fun recoverableFail(message: String, isMissingMolecularResultForEvaluation: Boolean = false): Evaluation {
        return fail(message, true, isMissingMolecularResultForEvaluation)
    }

    fun undetermined(
        message: String,
        recoverable: Boolean = false,
        isMissingMolecularResultForEvaluation: Boolean = false
    ): Evaluation {
        return Evaluation(
            recoverable = recoverable,
            result = EvaluationResult.UNDETERMINED,
            undeterminedMessages = messages(message),
            isMissingMolecularResultForEvaluation = isMissingMolecularResultForEvaluation
        )
    }

    fun recoverableUndetermined(message: String): Evaluation {
        return undetermined(message, true)
    }

    fun warn(
        message: String,
        inclusionEvents: Set<String> = emptySet(),
        isMissingMolecularResultForEvaluation: Boolean = false
    ): Evaluation {
        return Evaluation(
            recoverable = false,
            result = EvaluationResult.WARN,
            warnMessages = messages(message),
            inclusionMolecularEvents = inclusionEvents,
            isMissingMolecularResultForEvaluation = isMissingMolecularResultForEvaluation
        )
    }

    fun notEvaluated(message: String): Evaluation {
        return Evaluation(
            recoverable = false,
            result = EvaluationResult.NOT_EVALUATED,
            passMessages = messages(message)
        )
    }

    private fun messages(message: String) = setOf(StaticMessage(message))
}

