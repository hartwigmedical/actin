package com.hartwig.actin.algo.evaluation.composite

import com.hartwig.actin.algo.evaluation.EvaluationFunction
import com.hartwig.actin.datamodel.PatientRecord
import com.hartwig.actin.datamodel.algo.Evaluation

class And(private val functions: List<EvaluationFunction>) : EvaluationFunction {

    override fun evaluate(record: PatientRecord): Evaluation {
        val evaluationsByResult = functions.map { it.evaluate(record) }.distinct().groupBy(Evaluation::result)
        val worst = evaluationsByResult.keys.minOrNull()
            ?: throw IllegalStateException("Could not determine AND result for functions: $functions")

        val (recoverableEvaluations, unrecoverableEvaluations) = evaluationsByResult[worst]!!.partition(Evaluation::recoverable)
        val recoverable = unrecoverableEvaluations.isEmpty()
        val evaluations = if (recoverable) recoverableEvaluations else unrecoverableEvaluations

        return evaluations.fold(Evaluation(worst, recoverable), Evaluation::addMessagesAndEvents)
    }
}