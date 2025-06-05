package com.hartwig.actin.algo.evaluation.composite

import com.hartwig.actin.algo.evaluation.EvaluationFunction
import com.hartwig.actin.datamodel.PatientRecord
import com.hartwig.actin.datamodel.algo.Evaluation
import com.hartwig.actin.datamodel.algo.EvaluationResult

class Or(private val functions: List<EvaluationFunction>) : EvaluationFunction {

    override fun evaluate(record: PatientRecord): Evaluation {
        return combineOrEvaluations(functions.map { it.evaluate(record) })
    }
}

fun combineOrEvaluations(evaluations: List<Evaluation>): Evaluation {
    val evaluationsByResult = evaluations.distinct().groupBy(Evaluation::result)
    val bestResult = evaluationsByResult.keys.maxOrNull()
        ?: throw IllegalStateException("Could not determine OR result")

    val additionalEvaluations = if (bestResult == EvaluationResult.PASS) {
        evaluationsByResult[EvaluationResult.WARN]?.filter { it.exclusionMolecularEvents.isNotEmpty() || it.inclusionMolecularEvents.isNotEmpty() }
            ?: emptyList()
    } else emptyList()
    val evaluations = evaluationsByResult[bestResult]!! + additionalEvaluations
    val recoverable = evaluations.any(Evaluation::recoverable)
    val filteredEvaluations =
        if (bestResult == EvaluationResult.FAIL && recoverable) evaluations.filter { it.recoverable } else evaluations

    return filteredEvaluations.fold(Evaluation(bestResult, recoverable), Evaluation::addMessagesAndEvents)
}