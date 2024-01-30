package com.hartwig.actin.algo.datamodel

data class Evaluation(
    val result: EvaluationResult,
    val recoverable: Boolean,
    val inclusionMolecularEvents: Set<String> = emptySet(),
    val exclusionMolecularEvents: Set<String> = emptySet(),
    val passSpecificMessages: Set<String> = emptySet(),
    val passGeneralMessages: Set<String> = emptySet(),
    val warnSpecificMessages: Set<String> = emptySet(),
    val warnGeneralMessages: Set<String> = emptySet(),
    val undeterminedSpecificMessages: Set<String> = emptySet(),
    val undeterminedGeneralMessages: Set<String> = emptySet(),
    val failSpecificMessages: Set<String> = emptySet(),
    val failGeneralMessages: Set<String> = emptySet()
) {

    fun addMessagesAndEvents(other: Evaluation): Evaluation {
        return Evaluation(
            result = result,
            recoverable = recoverable,
            inclusionMolecularEvents = inclusionMolecularEvents + other.inclusionMolecularEvents,
            exclusionMolecularEvents = exclusionMolecularEvents + other.exclusionMolecularEvents,
            passSpecificMessages = passSpecificMessages + other.passSpecificMessages,
            passGeneralMessages = passGeneralMessages + other.passGeneralMessages,
            warnSpecificMessages = warnSpecificMessages + other.warnSpecificMessages,
            warnGeneralMessages = warnGeneralMessages + other.warnGeneralMessages,
            undeterminedSpecificMessages = undeterminedSpecificMessages + other.undeterminedSpecificMessages,
            undeterminedGeneralMessages = undeterminedGeneralMessages + other.undeterminedGeneralMessages,
            failSpecificMessages = failSpecificMessages + other.failSpecificMessages,
            failGeneralMessages = failGeneralMessages + other.failGeneralMessages
        )
    }
}
