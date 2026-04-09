package com.hartwig.actin.algo.evaluation

import com.hartwig.actin.algo.evaluation.molecular.toMolecularEvents
import com.hartwig.actin.datamodel.algo.Evaluation
import com.hartwig.actin.datamodel.algo.EvaluationResult
import com.hartwig.actin.datamodel.algo.MolecularEvent
import com.hartwig.actin.datamodel.algo.StaticMessage

object EvaluationFactory {

    @JvmName("passWithMolecularEvents")
    fun pass(message: String, recoverable: Boolean = false, inclusionEvents: Set<MolecularEvent> = emptySet()): Evaluation {
        return Evaluation(
            recoverable = recoverable,
            result = EvaluationResult.PASS,
            passMessages = messages(message),
            inclusionMolecularEvents = inclusionEvents
        )
    }

    @JvmName("passWithStringEvents")
    fun pass(message: String, recoverable: Boolean = false, inclusionEvents: Set<String> = emptySet()): Evaluation {
        return pass(message, recoverable, inclusionEvents.toMolecularEvents())
    }

    fun pass(message: String, recoverable: Boolean = false): Evaluation {
        return pass(message, recoverable, emptySet<MolecularEvent>())
    }

    fun recoverablePass(message: String): Evaluation {
        return pass(message, true, emptySet<MolecularEvent>())
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

    @JvmName("warnWithMolecularEvents")
    fun warn(
        message: String,
        inclusionEvents: Set<MolecularEvent> = emptySet(),
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

    @JvmName("passWithStringEvents")
    fun warn(
        message: String,
        inclusionEvents: Set<String> = emptySet(),
        isMissingMolecularResultForEvaluation: Boolean = false
    ): Evaluation {
        return warn(message, inclusionEvents.toMolecularEvents(), isMissingMolecularResultForEvaluation)
    }

    fun warn(
        message: String,
        isMissingMolecularResultForEvaluation: Boolean = false
    ): Evaluation {
        return warn(message, emptySet<MolecularEvent>(), isMissingMolecularResultForEvaluation)
    }

    private fun messages(message: String) = setOf(StaticMessage(message))
}

