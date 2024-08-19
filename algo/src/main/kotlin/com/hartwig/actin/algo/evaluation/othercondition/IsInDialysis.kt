package com.hartwig.actin.algo.evaluation.othercondition

import com.hartwig.actin.PatientRecord
import com.hartwig.actin.algo.datamodel.Evaluation
import com.hartwig.actin.algo.evaluation.EvaluationFactory
import com.hartwig.actin.algo.evaluation.EvaluationFunction

class IsInDialysis: EvaluationFunction {
    override fun evaluate(record: PatientRecord): Evaluation {
        return EvaluationFactory.notEvaluated(
            "Currently assumed that patient won't be in dialysis",
            "Assumed patient is not in dialysis"
        )
    }
}