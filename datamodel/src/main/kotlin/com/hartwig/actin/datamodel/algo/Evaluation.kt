package com.hartwig.actin.datamodel.algo

data class Evaluation(
    val result: EvaluationResult,
    val recoverable: Boolean,
    val inclusionMolecularEvents: Set<String> = emptySet(),
    val exclusionMolecularEvents: Set<String> = emptySet(),
    val passMessages: Set<String> = emptySet(),
    val warnMessages: Set<String> = emptySet(),
    val undeterminedMessages: Set<String> = emptySet(),
    val failMessages: Set<String> = emptySet(),
    val isMissingGenesForSufficientEvaluation: Boolean = false
) {

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
            isMissingGenesForSufficientEvaluation = isMissingGenesForSufficientEvaluation || other.isMissingGenesForSufficientEvaluation
        )
    }
}
