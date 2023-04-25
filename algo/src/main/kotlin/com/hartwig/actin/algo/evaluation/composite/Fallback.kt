package com.hartwig.actin.algo.evaluation.composite

import com.hartwig.actin.PatientRecord
import com.hartwig.actin.algo.datamodel.Evaluation
import com.hartwig.actin.algo.datamodel.EvaluationResult
import com.hartwig.actin.algo.evaluation.EvaluationFunction

class Fallback(private val primary: EvaluationFunction, private val secondary: EvaluationFunction) : EvaluationFunction {
    override fun evaluate(record: PatientRecord): Evaluation {
        val primaryEvaluation = primary.evaluate(record)
        return if (primaryEvaluation.result() != EvaluationResult.UNDETERMINED) primaryEvaluation else secondary.evaluate(record)
    }
}