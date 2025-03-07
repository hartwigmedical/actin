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

    fun fail(message: String, recoverable: Boolean = false, isMissingMolecularResultForEvaluation: Boolean = false): Evaluation {
        return Evaluation(
            recoverable = recoverable,
            result = EvaluationResult.FAIL,
            failMessages = setOf(message),
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
            undeterminedMessages = setOf(message),
            isMissingMolecularResultForEvaluation = isMissingMolecularResultForEvaluation
        )
    }

    fun recoverableUndetermined(message: String): Evaluation {
        return undetermined(message, true)
    }

    fun warn(
        message: String,
        recoverable: Boolean = false,
        inclusionEvents: Set<String> = emptySet(),
        isMissingMolecularResultForEvaluation: Boolean = false
    ): Evaluation {
        return Evaluation(
            recoverable = recoverable,
            result = EvaluationResult.WARN,
            warnMessages = setOf(message),
            inclusionMolecularEvents = inclusionEvents,
            isMissingMolecularResultForEvaluation = isMissingMolecularResultForEvaluation
        )
    }

    fun recoverableWarn(message: String): Evaluation {
        return warn(message, true)
    }

    fun notEvaluated(message: String): Evaluation {
        return Evaluation(
            recoverable = false,
            result = EvaluationResult.NOT_EVALUATED,
            passMessages = setOf(message)
        )
    }
}

