package com.hartwig.actin.algo.evaluation.general

import com.hartwig.actin.PatientRecord
import com.hartwig.actin.algo.datamodel.Evaluation
import com.hartwig.actin.algo.datamodel.EvaluationResult
import com.hartwig.actin.algo.evaluation.EvaluationFactory.unrecoverable
import com.hartwig.actin.algo.evaluation.EvaluationFunction

class HasSufficientLifeExpectancy internal constructor() : EvaluationFunction {
    override fun evaluate(record: PatientRecord): Evaluation {
        return unrecoverable()
            .result(EvaluationResult.NOT_EVALUATED)
            .addPassSpecificMessages("Currently assumed that requested life expectancy will be met")
            .addPassGeneralMessages("Sufficient life expectancy is assumed")
            .build()
    }
}