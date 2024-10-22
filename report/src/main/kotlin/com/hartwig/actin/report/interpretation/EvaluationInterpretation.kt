package com.hartwig.actin.report.interpretation

import com.hartwig.actin.datamodel.algo.EvaluationResult

data class EvaluationInterpretation(
    val rule: String,
    val reference: String,
    val entriesPerResult: Map<EvaluationResult, EvaluationEntry>
)

data class EvaluationEntry(
    val header: String,
    val messages: Set<String>
)
