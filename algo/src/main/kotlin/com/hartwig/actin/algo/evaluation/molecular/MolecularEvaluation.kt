package com.hartwig.actin.algo.evaluation.molecular

import com.hartwig.actin.algo.datamodel.Evaluation
import com.hartwig.actin.algo.datamodel.EvaluationResult
import com.hartwig.actin.molecular.datamodel.ExperimentType
import com.hartwig.actin.molecular.datamodel.MolecularTest

class MolecularEvaluation(
    private val evaluations: List<Pair<MolecularTest<*>, Evaluation>> = emptyList(),
    private val defaultUndetermined: Evaluation,
) {

    fun combined(): Evaluation {
        val groupedEvaluationsByResult = evaluations
            .groupBy { evaluation -> evaluation.second.result }

        return (groupedEvaluationsByResult[EvaluationResult.PASS]
            ?: groupedEvaluationsByResult[EvaluationResult.WARN]
            ?: groupedEvaluationsByResult[EvaluationResult.FAIL])
            ?.sortedBy { it.first.date }
            ?.map { convertEvaluation(it) }
            ?.reduce { acc, pair -> acc.addMessagesAndEvents(pair) }
            ?: defaultUndetermined
    }

    private fun convertEvaluation(evaluation: Pair<MolecularTest<*>, Evaluation>): Evaluation {
        return evaluation.second.copy(
            inclusionMolecularEvents = convertEvents(evaluation.second.inclusionMolecularEvents, evaluation.first.type),
            exclusionMolecularEvents = convertEvents(evaluation.second.inclusionMolecularEvents, evaluation.first.type)
    }

    private fun convertEvents(events: Set<String>, type: ExperimentType) =
        events.map { event(type, it) }.toSet()

    private fun event(type: ExperimentType, event: String): String {
        return "$event ($type)"
    }
}