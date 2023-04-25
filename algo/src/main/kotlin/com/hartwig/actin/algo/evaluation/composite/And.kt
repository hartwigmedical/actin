package com.hartwig.actin.algo.evaluation.composite

import com.google.common.collect.Sets
import com.hartwig.actin.PatientRecord
import com.hartwig.actin.algo.datamodel.Evaluation
import com.hartwig.actin.algo.datamodel.EvaluationResult
import com.hartwig.actin.algo.datamodel.ImmutableEvaluation
import com.hartwig.actin.algo.evaluation.EvaluationFunction

class And(private val functions: List<EvaluationFunction>) : EvaluationFunction {
    override fun evaluate(record: PatientRecord): Evaluation {
        val evaluations: MutableSet<Evaluation> = Sets.newHashSet()
        for (function in functions) {
            evaluations.add(function.evaluate(record))
        }
        var worst: EvaluationResult? = null
        var recoverable: Boolean? = null
        for (eval in evaluations) {
            if (worst == null || eval.result().isWorseThan(worst)) {
                worst = eval.result()
                recoverable = eval.recoverable()
            } else if (worst == eval.result()) {
                recoverable = eval.recoverable() && recoverable!!
            }
        }
        check(!(worst == null || recoverable == null)) { "Could not determine AND result for functions: $functions" }
        val builder = ImmutableEvaluation.builder().result(worst).recoverable(recoverable)
        for (eval in evaluations) {
            if (eval.result() == worst && eval.recoverable() == recoverable) {
                builder.addAllInclusionMolecularEvents(eval.inclusionMolecularEvents())
                builder.addAllExclusionMolecularEvents(eval.exclusionMolecularEvents())
                builder.addAllPassSpecificMessages(eval.passSpecificMessages())
                builder.addAllPassGeneralMessages(eval.passGeneralMessages())
                builder.addAllWarnSpecificMessages(eval.warnSpecificMessages())
                builder.addAllWarnGeneralMessages(eval.warnGeneralMessages())
                builder.addAllUndeterminedSpecificMessages(eval.undeterminedSpecificMessages())
                builder.addAllUndeterminedGeneralMessages(eval.undeterminedGeneralMessages())
                builder.addAllFailSpecificMessages(eval.failSpecificMessages())
                builder.addAllFailGeneralMessages(eval.failGeneralMessages())
            }
        }
        return builder.build()
    }
}