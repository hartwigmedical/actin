package com.hartwig.actin.algo.evaluation.composite

import com.hartwig.actin.PatientRecord
import com.hartwig.actin.algo.datamodel.Evaluation
import com.hartwig.actin.algo.datamodel.EvaluationResult
import com.hartwig.actin.algo.evaluation.EvaluationFunction

class Or(functions: List<EvaluationFunction>) : EvaluationFunction {
    private val functions: List<EvaluationFunction>

    init {
        this.functions = functions
    }

    override fun evaluate(record: PatientRecord): Evaluation {
        val evaluations = functions.map { it.evaluate(record) }.toSet()
        var best: EvaluationResult? = null
        var recoverable: Boolean? = null
        for (eval in evaluations) {
            if (best == null || best.isWorseThan(eval.result)) {
                best = eval.result
                recoverable = eval.recoverable
            } else if (best == eval.result) {
                recoverable = eval.recoverable || recoverable!!
            }
        }
        check(!(best == null || recoverable == null)) { "Could not determine OR result for functions: $functions" }
        return evaluations.filter { it.result == best }
            .fold(Evaluation(best, recoverable)) { acc, eval -> acc.addMessagesAndEvents(eval) }
    }
}