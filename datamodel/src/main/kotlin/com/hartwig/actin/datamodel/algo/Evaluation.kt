package com.hartwig.actin.datamodel.algo

interface EvaluationMessage {
    fun combineBy(): String
    fun combine(other: EvaluationMessage): EvaluationMessage
}

data class StaticMessage(val message: String) : EvaluationMessage {

    override fun combineBy(): String {
        return message
    }

    override fun combine(other: EvaluationMessage): EvaluationMessage {
        return this
    }

    override fun toString(): String {
        return message
    }
}

data class Evaluation(
    val result: EvaluationResult,
    val recoverable: Boolean,
    val inclusionMolecularEvents: Set<String> = emptySet(),
    val exclusionMolecularEvents: Set<String> = emptySet(),
    val passMessages: Set<EvaluationMessage> = emptySet(),
    val warnMessages: Set<EvaluationMessage> = emptySet(),
    val undeterminedMessages: Set<EvaluationMessage> = emptySet(),
    val failMessages: Set<EvaluationMessage> = emptySet(),
    val isMissingMolecularResultForEvaluation: Boolean = false
) {

    fun passMessagesStrings() = passMessages.map { it.toString() }.toSet()
    fun warnMessagesStrings() = warnMessages.map { it.toString() }.toSet()
    fun undeterminedMessagesStrings() = undeterminedMessages.map { it.toString() }.toSet()
    fun failMessagesStrings() = failMessages.map { it.toString() }.toSet()

    fun addMessagesAndEvents(other: Evaluation): Evaluation {
        return Evaluation(
            result = result,
            recoverable = recoverable,
            inclusionMolecularEvents = inclusionMolecularEvents + other.inclusionMolecularEvents,
            exclusionMolecularEvents = exclusionMolecularEvents + other.exclusionMolecularEvents,
            passMessages = passMessages + other.passMessages,
            warnMessages = warnMessages + other.warnMessages,
            undeterminedMessages = undeterminedMessages + other.undeterminedMessages,
            failMessages = failMessages + other.failMessages,
            isMissingMolecularResultForEvaluation = if (result == EvaluationResult.PASS) false else isMissingMolecularResultForEvaluation || other.isMissingMolecularResultForEvaluation
        )
    }
}
