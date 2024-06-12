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
        fun combine(evaluations: List<MolecularEvaluation>, fallbackUndetermined: Evaluation): Evaluation {

            val groupedEvaluationsByResult = evaluations
                .groupBy { evaluation -> evaluation.evaluation.result }

            val sortedBy = (groupedEvaluationsByResult[EvaluationResult.PASS]
                ?: groupedEvaluationsByResult[EvaluationResult.WARN]
                ?: groupedEvaluationsByResult[EvaluationResult.FAIL]
                ?: groupedEvaluationsByResult[EvaluationResult.UNDETERMINED])
                ?.asSequence()
                ?.sortedByDescending { it.test.date }
                ?.sortedBy {
                    when (it.test.type) {
                        ExperimentType.WHOLE_GENOME -> 1
                        ExperimentType.TARGETED -> 2
                        else -> 3
                    }
                }

            return sortedBy?.let {
                if (isOrangeResult(it)) return it.first().evaluation else it.map { m -> m.evaluation }
                    .reduce(Evaluation::addMessagesAndEvents)
            } ?: fallbackUndetermined
        }

        private fun isOrangeResult(it: Sequence<MolecularEvaluation>) =
            it.first().test.type in setOf(
                ExperimentType.WHOLE_GENOME,
                ExperimentType.TARGETED
            )

    }
}