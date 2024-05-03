package com.hartwig.actin.algo.evaluation.composite

import com.hartwig.actin.PatientRecord
import com.hartwig.actin.algo.datamodel.Evaluation
import com.hartwig.actin.algo.datamodel.EvaluationResult
import com.hartwig.actin.algo.evaluation.EvaluationFunction

class Or(private val functions: List<EvaluationFunction>) : EvaluationFunction {

    override fun evaluate(record: PatientRecord): Evaluation {
        val evaluationsByResult = functions.map { it.evaluate(record) }.distinct().groupBy(Evaluation::result)
        val best = evaluationsByResult.keys.maxOrNull()
            ?: throw IllegalStateException("Could not determine OR result for functions: $functions")

        val evaluations = evaluationsByResult[best]!!

        val evaluation = evaluations.fold(Evaluation(best, evaluations.any(Evaluation::recoverable)), Evaluation::addMessagesAndEvents)

        return if (best == EvaluationResult.PASS) {
            evaluation.copy(
                inclusionMolecularEvents = evaluation.inclusionMolecularEvents +
                        (evaluationsByResult[EvaluationResult.WARN]?.flatMap(Evaluation::inclusionMolecularEvents) ?: emptyList())
            )
        } else evaluation
    }
}