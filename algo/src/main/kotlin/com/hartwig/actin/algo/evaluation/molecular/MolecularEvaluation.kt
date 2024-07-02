package com.hartwig.actin.algo.evaluation.molecular

import com.hartwig.actin.algo.datamodel.Evaluation
import com.hartwig.actin.algo.datamodel.EvaluationResult
import com.hartwig.actin.molecular.datamodel.ExperimentType
import com.hartwig.actin.molecular.datamodel.MolecularTest

data class MolecularEvaluation(
    val test: MolecularTest,
    val evaluation: Evaluation
) {
    companion object {

        private fun defaultEvaluationPrecedence(groupedEvaluationsByResult: Map<EvaluationResult, List<MolecularEvaluation>>) =
            (groupedEvaluationsByResult[EvaluationResult.PASS]
                ?: groupedEvaluationsByResult[EvaluationResult.WARN]
                ?: groupedEvaluationsByResult[EvaluationResult.FAIL]
                ?: groupedEvaluationsByResult[EvaluationResult.UNDETERMINED])

        fun combine(
            evaluations: List<MolecularEvaluation?>,
            precedence: (Map<EvaluationResult, List<MolecularEvaluation>>) -> List<MolecularEvaluation>? = ::defaultEvaluationPrecedence
        ): Evaluation {

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

            val sortedPreferredEvaluations = precedence.invoke(groupedEvaluationsByResult)?.sortedWith(evaluationComparator)

            return sortedPreferredEvaluations?.let {
                if (isOrangeResult(it)) it.first().evaluation else
                    it.map { m -> m.evaluation }.reduce(Evaluation::addMessagesAndEvents)
            } ?: throw IllegalStateException("Unable to combine molecular evaluations [$evaluations]")
        }

        private fun isOrangeResult(it: List<MolecularEvaluation>) =
            it.first().test.type in setOf(
                ExperimentType.WHOLE_GENOME,
                ExperimentType.TARGETED
            )

    }
}