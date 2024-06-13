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
        fun combine(evaluations: List<MolecularEvaluation?>, fallbackUndetermined: Evaluation): Evaluation {

            val groupedEvaluationsByResult = evaluations.filterNotNull()
                .groupBy { evaluation -> evaluation.evaluation.result }

            val evaluationComparator = Comparator.comparing<MolecularEvaluation, Int> {
                when (it.test.type) {
                    ExperimentType.WHOLE_GENOME -> 1
                    ExperimentType.TARGETED -> 2
                    else -> 3
                }
            }
                .thenByDescending { it.test.date }

            val sortedPreferredEvaluations = (groupedEvaluationsByResult[EvaluationResult.PASS]
                ?: groupedEvaluationsByResult[EvaluationResult.WARN]
                ?: groupedEvaluationsByResult[EvaluationResult.FAIL]
                ?: groupedEvaluationsByResult[EvaluationResult.UNDETERMINED])
                ?.sortedWith(evaluationComparator)

            return sortedPreferredEvaluations?.let {
                if (isOrangeResult(it)) it.first().evaluation else
                    it.map { m -> m.evaluation }.reduce(Evaluation::addMessagesAndEvents)
            } ?: fallbackUndetermined
        }

        private fun isOrangeResult(it: List<MolecularEvaluation>) =
            it.first().test.type in setOf(
                ExperimentType.WHOLE_GENOME,
                ExperimentType.TARGETED
            )

    }
}