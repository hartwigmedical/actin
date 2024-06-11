package com.hartwig.actin.algo.evaluation.molecular

import com.hartwig.actin.algo.datamodel.Evaluation
import com.hartwig.actin.algo.datamodel.EvaluationResult
import com.hartwig.actin.molecular.datamodel.ExperimentType
import com.hartwig.actin.molecular.datamodel.MolecularTest

data class MolecularEvaluation(
    val test: MolecularTest<*>,
    val evaluation: Evaluation
) {
    companion object {
        fun combined(evaluations: List<MolecularEvaluation>, defaultUndetermined: Evaluation): Evaluation {

            val groupedEvaluationsByResult = evaluations
                .groupBy { evaluation -> evaluation.evaluation.result }

            return (groupedEvaluationsByResult[EvaluationResult.PASS]
                ?: groupedEvaluationsByResult[EvaluationResult.WARN]
                ?: groupedEvaluationsByResult[EvaluationResult.FAIL])
                ?.asSequence()
                ?.sortedByDescending { it.test.date }
                ?.map { convertEvaluation(it) }
                ?.sortedBy { it.test.type.ordinal }
                ?.map { it.evaluation }
                ?.first()
                ?: defaultUndetermined
        }

        private fun convertEvaluation(evaluation: MolecularEvaluation): MolecularEvaluation {
            return evaluation.copy(
                evaluation = evaluation.evaluation.copy(
                    inclusionMolecularEvents = convertEvents(evaluation.evaluation.inclusionMolecularEvents, evaluation.test.type),
                    exclusionMolecularEvents = convertEvents(evaluation.evaluation.inclusionMolecularEvents, evaluation.test.type)
                )
            )
        }

        private fun convertEvents(events: Set<String>, type: ExperimentType) =
            events.map { event(type, it) }.toSet()

        private fun event(type: ExperimentType, event: String): String {
            return event
        }
    }
}