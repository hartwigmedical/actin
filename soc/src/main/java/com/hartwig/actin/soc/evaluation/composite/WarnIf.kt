package com.hartwig.actin.soc.evaluation.composite

import com.hartwig.actin.PatientRecord
import com.hartwig.actin.algo.datamodel.Evaluation
import com.hartwig.actin.algo.datamodel.EvaluationResult
import com.hartwig.actin.algo.datamodel.ImmutableEvaluation
import com.hartwig.actin.soc.evaluation.EvaluationFunction

class WarnIf(function: EvaluationFunction) : EvaluationFunction {
    private val function: EvaluationFunction

    init {
        this.function = function
    }

    override fun evaluate(record: PatientRecord): Evaluation {
        val evaluation: Evaluation = function.evaluate(record)
        if (evaluation.result() == EvaluationResult.PASS) {
            return ImmutableEvaluation.builder()
                    .result(EvaluationResult.WARN)
                    .recoverable(evaluation.recoverable())
                    .inclusionMolecularEvents(evaluation.inclusionMolecularEvents())
                    .exclusionMolecularEvents(evaluation.exclusionMolecularEvents())
                    .warnSpecificMessages(evaluation.passSpecificMessages())
                    .warnGeneralMessages(evaluation.passGeneralMessages())
                    .build()
        } else if (evaluation.result() == EvaluationResult.WARN) {
            return evaluation
        }
        val builder: ImmutableEvaluation.Builder = ImmutableEvaluation.builder()
                .result(EvaluationResult.PASS)
                .recoverable(evaluation.recoverable())
                .addAllInclusionMolecularEvents(evaluation.inclusionMolecularEvents())
                .addAllExclusionMolecularEvents(evaluation.exclusionMolecularEvents())
                .addAllPassSpecificMessages(evaluation.passSpecificMessages())
                .addAllPassSpecificMessages(evaluation.warnSpecificMessages())
                .addAllPassSpecificMessages(evaluation.undeterminedSpecificMessages())
                .addAllPassSpecificMessages(evaluation.failSpecificMessages())
                .addAllPassGeneralMessages(evaluation.passGeneralMessages())
                .addAllPassGeneralMessages(evaluation.warnGeneralMessages())
                .addAllPassGeneralMessages(evaluation.undeterminedGeneralMessages())
                .addAllPassGeneralMessages(evaluation.failGeneralMessages())
        if (evaluation.result() == EvaluationResult.NOT_IMPLEMENTED) {
            builder.addPassGeneralMessages("not implemented")
            builder.addPassSpecificMessages("not implemented")
        }
        return builder.build()
    }
}