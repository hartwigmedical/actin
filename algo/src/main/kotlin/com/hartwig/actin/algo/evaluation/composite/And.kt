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

        val unrecoverableEvaluations = evaluationsByResult[worstResult]!!.filterNot(Evaluation::recoverable)
        val recoverable = unrecoverableEvaluations.isEmpty()
        val additionalEvaluations = listOf(EvaluationResult.PASS, EvaluationResult.WARN)
            .flatMap { result ->
                evaluationsByResult[result]?.filter { it.exclusionMolecularEvents.isNotEmpty() || it.inclusionMolecularEvents.isNotEmpty() }
                    ?: emptyList()
            }

        return when {
            worstResult == EvaluationResult.FAIL && !recoverable -> {
                val result = unrecoverableEvaluations.fold(Evaluation(EvaluationResult.FAIL, false), Evaluation::addMessagesAndEvents)
                result.copy(
                    inclusionMolecularEvents = result.inclusionMolecularEvents + additionalEvaluations.flatMap { it.inclusionMolecularEvents },
                    exclusionMolecularEvents = result.exclusionMolecularEvents + additionalEvaluations.flatMap { it.exclusionMolecularEvents }
                )
            }

            worstResult == EvaluationResult.UNDETERMINED && recoverable -> {
                evaluationsByResult.flatMap { it.value }
                    .fold(Evaluation(EvaluationResult.UNDETERMINED, true), Evaluation::addMessagesAndEvents)
            }

            else -> {
                evaluationsByResult.flatMap { it.value }
                    .filterNot { it.recoverable && it.result == EvaluationResult.UNDETERMINED }
                    .fold(Evaluation(worstResult, recoverable), Evaluation::addMessagesAndEvents)
            }
        }
    }
}