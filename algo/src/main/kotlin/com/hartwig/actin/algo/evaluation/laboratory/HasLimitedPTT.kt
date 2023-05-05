package com.hartwig.actin.algo.evaluation.laboratory

import com.hartwig.actin.PatientRecord
import com.hartwig.actin.algo.datamodel.Evaluation
import com.hartwig.actin.algo.datamodel.EvaluationResult
import com.hartwig.actin.algo.evaluation.EvaluationFactory.recoverable
import com.hartwig.actin.algo.evaluation.EvaluationFunction

class HasLimitedPTT internal constructor() : EvaluationFunction {
    override fun evaluate(record: PatientRecord): Evaluation {
        return recoverable()
            .result(EvaluationResult.UNDETERMINED)
            .addUndeterminedSpecificMessages("PTT cannot be determined yet")
            .build()
    }
}