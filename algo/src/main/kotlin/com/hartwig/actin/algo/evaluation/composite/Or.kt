package com.hartwig.actin.algo.evaluation.composite

import com.hartwig.actin.algo.evaluation.EvaluationFunction
import com.hartwig.actin.datamodel.PatientRecord
import com.hartwig.actin.datamodel.algo.Evaluation
import com.hartwig.actin.datamodel.algo.EvaluationResult

class Or(private val functions: List<EvaluationFunction>) : EvaluationFunction {

    override fun evaluate(record: PatientRecord): Evaluation {
        val evaluationsByResult = functions.map { it.evaluate(record) }.distinct().groupBy(Evaluation::result)
        val evaluationResult = evaluationsByResult.keys.maxOrNull()
            ?: throw IllegalStateException("Could not determine OR result for functions: $functions")

        val additionalEvaluations = if (evaluationResult == EvaluationResult.PASS) {
            evaluationsByResult[EvaluationResult.WARN]?.filter { it.exclusionMolecularEvents.isNotEmpty() || it.inclusionMolecularEvents.isNotEmpty() }
                ?: emptyList()
        } else emptyList()
        val evaluations = evaluationsByResult[evaluationResult]!! + additionalEvaluations
        val filteredEvaluations =
            if (evaluationResult == EvaluationResult.FAIL && evaluations.any { it.recoverable }) evaluations.filter { it.recoverable } else evaluations

        return filteredEvaluations.fold(
            Evaluation(evaluationResult, filteredEvaluations.any(Evaluation::recoverable)),
            Evaluation::addMessagesAndEvents
        )
    }
}