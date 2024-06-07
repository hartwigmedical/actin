package com.hartwig.actin.algo.evaluation.molecular

import com.hartwig.actin.algo.datamodel.Evaluation
import com.hartwig.actin.algo.datamodel.EvaluationResult

class MolecularEvaluation(
    private val molecularRecordEvaluation: Evaluation? = null,
    private val panelEvaluations: List<Evaluation> = emptyList(),
    private val defaultUndetermined: Evaluation,
) {

    fun combined(): Evaluation {
        if (molecularRecordEvaluation?.result == EvaluationResult.PASS) {
            return molecularRecordEvaluation
        }
        val groupedEvaluationsByResult =
            (panelEvaluations + listOfNotNull(molecularRecordEvaluation))
                .groupBy { evaluation -> evaluation.result }
                .mapValues { entry ->
                    entry.value.reduce { acc, y -> acc.addMessagesAndEvents(y) }
                }
        return groupedEvaluationsByResult[EvaluationResult.PASS]
            ?: groupedEvaluationsByResult[EvaluationResult.WARN]
            ?: groupedEvaluationsByResult[EvaluationResult.FAIL]
            ?: defaultUndetermined
    }
}