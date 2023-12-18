package com.hartwig.actin.algo.evaluation.general

import com.hartwig.actin.PatientRecord
import com.hartwig.actin.algo.datamodel.Evaluation
import com.hartwig.actin.algo.evaluation.EvaluationFactory
import com.hartwig.actin.algo.evaluation.EvaluationFunction

class HasSufficientLifeExpectancy : EvaluationFunction {
    
    override fun evaluate(record: PatientRecord): Evaluation {
        return EvaluationFactory.notEvaluated(
            "Currently assumed that requested life expectancy will be met", "Sufficient life expectancy is assumed"
        )
    }
}