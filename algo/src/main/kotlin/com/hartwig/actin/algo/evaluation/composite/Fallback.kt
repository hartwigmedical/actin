package com.hartwig.actin.algo.evaluation.composite

import com.hartwig.actin.algo.evaluation.EvaluationFunction
import com.hartwig.actin.datamodel.PatientRecord
import com.hartwig.actin.datamodel.algo.Evaluation
import com.hartwig.actin.datamodel.algo.EvaluationResult

class Fallback(private val primary: EvaluationFunction, private val secondary: EvaluationFunction) : EvaluationFunction {

    override fun evaluate(record: PatientRecord): Evaluation {
        val primaryEvaluation = primary.evaluate(record)
        return if (primaryEvaluation.result != EvaluationResult.UNDETERMINED) primaryEvaluation else secondary.evaluate(record)
    }
}