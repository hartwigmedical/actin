package com.hartwig.actin.algo.evaluation

import com.hartwig.actin.algo.datamodel.Evaluation
import com.hartwig.actin.algo.datamodel.EvaluationResult

object EvaluationFactory {

    fun pass(
        specificMessage: String, generalMessage: String? = null, recoverable: Boolean = false, inclusionEvents: Set<String> = emptySet()
    ): Evaluation {
        return Evaluation(
            recoverable = recoverable,
            result = EvaluationResult.PASS,
            passSpecificMessages = setOf(specificMessage),
            passGeneralMessages = setOf(generalMessage ?: specificMessage),
            inclusionMolecularEvents = inclusionEvents
        )
    }

    fun recoverablePass(specificMessage: String, generalMessage: String? = null): Evaluation {
        return pass(specificMessage, generalMessage, true)
    }

    fun fail(specificMessage: String, generalMessage: String? = null, recoverable: Boolean = false): Evaluation {
        return createFail(recoverable, specificMessage, setOf(generalMessage ?: specificMessage))
    }

    fun failNoGeneral(specificMessage: String, recoverable: Boolean = false): Evaluation {
        return createFail(recoverable, specificMessage, emptySet())
    }

    fun recoverableFail(specificMessage: String, generalMessage: String? = null): Evaluation {
        return fail(specificMessage, generalMessage, true)
    }

    fun undetermined(specificMessage: String, generalMessage: String? = null, recoverable: Boolean = false): Evaluation {
        return createUndetermined(recoverable, specificMessage, setOf(generalMessage ?: specificMessage))
    }

    fun recoverableUndetermined(specificMessage: String, generalMessage: String? = null): Evaluation {
        return undetermined(specificMessage, generalMessage, true)
    }

    fun undeterminedNoGeneral(specificMessage: String, recoverable: Boolean = false): Evaluation {
        return createUndetermined(recoverable, specificMessage, emptySet())
    }

    fun recoverableUndeterminedNoGeneral(specificMessage: String): Evaluation {
        return undeterminedNoGeneral(specificMessage, true)
    }

    fun warn(
        specificMessage: String, generalMessage: String? = null, recoverable: Boolean = false, inclusionEvents: Set<String> = emptySet()
    ): Evaluation {
        return Evaluation(
            recoverable = recoverable,
            result = EvaluationResult.WARN,
            warnSpecificMessages = setOf(specificMessage),
            warnGeneralMessages = setOf(generalMessage ?: specificMessage),
            inclusionMolecularEvents = inclusionEvents
        )
    }

    fun recoverableWarn(specificMessage: String, generalMessage: String? = null): Evaluation {
        return warn(specificMessage, generalMessage, true)
    }

    fun notEvaluated(specificMessage: String, generalMessage: String? = null): Evaluation {
        return createNotEvaluated(specificMessage, setOf(generalMessage ?: specificMessage))
    }

    fun notEvaluatedNoGeneral(specificMessage: String): Evaluation {
        return createNotEvaluated(specificMessage, emptySet())
    }

    private fun createFail(recoverable: Boolean, specificMessage: String, generalMessages: Set<String>) = Evaluation(
        recoverable = recoverable,
        result = EvaluationResult.FAIL,
        failSpecificMessages = setOf(specificMessage),
        failGeneralMessages = generalMessages
    )

    private fun createNotEvaluated(specificMessage: String, generalMessages: Set<String>) = Evaluation(
        recoverable = false,
        result = EvaluationResult.NOT_EVALUATED,
        passSpecificMessages = setOf(specificMessage),
        passGeneralMessages = generalMessages
    )

    private fun createUndetermined(recoverable: Boolean, specificMessage: String, generalMessages: Set<String>) = Evaluation(
        recoverable = recoverable,
        result = EvaluationResult.UNDETERMINED,
        undeterminedSpecificMessages = setOf(specificMessage),
        undeterminedGeneralMessages = generalMessages
    )
}