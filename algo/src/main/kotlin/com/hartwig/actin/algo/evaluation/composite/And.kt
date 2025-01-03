package com.hartwig.actin.algo.evaluation.composite

import com.hartwig.actin.algo.evaluation.EvaluationFunction
import com.hartwig.actin.datamodel.PatientRecord
import com.hartwig.actin.datamodel.algo.Evaluation
import com.hartwig.actin.datamodel.algo.EvaluationResult

class And(private val functions: List<EvaluationFunction>) : EvaluationFunction {

    override fun evaluate(record: PatientRecord): Evaluation {
        val evaluationsByResult = functions.map { it.evaluate(record) }.distinct().groupBy(Evaluation::result)
        val worstResult = evaluationsByResult.keys.minOrNull()
            ?: throw IllegalStateException("Could not determine AND result for functions: $functions")

        val (recoverableEvaluations, unrecoverableEvaluations) = evaluationsByResult[worstResult]!!.partition(Evaluation::recoverable)
        val recoverable = unrecoverableEvaluations.isEmpty()
        val evaluations = if (recoverable) recoverableEvaluations else unrecoverableEvaluations
        val additionalEvaluations = listOf(EvaluationResult.PASS, EvaluationResult.WARN)
            .flatMap { result ->
                evaluationsByResult[result]?.filter { it.exclusionMolecularEvents.isNotEmpty() || it.inclusionMolecularEvents.isNotEmpty() }
                    ?: emptyList()
            }

        return evaluations.fold(Evaluation(worstResult, recoverable), Evaluation::addMessagesAndEvents).let { result ->
            result.copy(
                inclusionMolecularEvents = result.inclusionMolecularEvents + additionalEvaluations.flatMap { it.inclusionMolecularEvents },
                exclusionMolecularEvents = result.exclusionMolecularEvents + additionalEvaluations.flatMap { it.exclusionMolecularEvents }
            )
        }
    }
}