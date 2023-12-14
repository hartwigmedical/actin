package com.hartwig.actin.algo.datamodel

data class Evaluation(
    val result: EvaluationResult,
    val recoverable: Boolean,
    val inclusionMolecularEvents: Set<String>,
    val exclusionMolecularEvents: Set<String>,
    val passSpecificMessages: Set<String>,
    val passGeneralMessages: Set<String>,
    val warnSpecificMessages: Set<String>,
    val warnGeneralMessages: Set<String>,
    val undeterminedSpecificMessages: Set<String>,
    val undeterminedGeneralMessages: Set<String>,
    val failSpecificMessages: Set<String>,
    val failGeneralMessages: Set<String>,
)
