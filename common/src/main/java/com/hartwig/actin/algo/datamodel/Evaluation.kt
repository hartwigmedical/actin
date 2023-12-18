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
)
