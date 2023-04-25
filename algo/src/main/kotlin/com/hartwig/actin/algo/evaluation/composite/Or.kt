package com.hartwig.actin.algo.evaluation.composite

import com.google.common.collect.Sets
import com.hartwig.actin.PatientRecord
import com.hartwig.actin.algo.datamodel.Evaluation
import com.hartwig.actin.algo.datamodel.EvaluationResult
import com.hartwig.actin.algo.datamodel.ImmutableEvaluation
import com.hartwig.actin.algo.evaluation.EvaluationFunction

class Or(private val functions: List<EvaluationFunction>) : EvaluationFunction {
    override fun evaluate(record: PatientRecord): Evaluation {
        val evaluations: MutableSet<Evaluation> = Sets.newHashSet()
        for (function in functions) {
            evaluations.add(function.evaluate(record))
        }
        var best: EvaluationResult? = null
        var recoverable: Boolean? = null
        for (eval in evaluations) {
            if (best == null || best.isWorseThan(eval.result())) {
                best = eval.result()
                recoverable = eval.recoverable()
            } else if (best == eval.result()) {
                recoverable = eval.recoverable() || recoverable!!
            }
        }
        check(!(best == null || recoverable == null)) { "Could not determine OR result for functions: $functions" }
        val builder = ImmutableEvaluation.builder().result(best).recoverable(recoverable)
        for (eval in evaluations) {
            if (eval.result() == best) {
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