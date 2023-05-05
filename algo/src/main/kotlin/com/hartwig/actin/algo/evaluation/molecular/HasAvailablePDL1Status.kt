package com.hartwig.actin.algo.evaluation.molecular

import com.hartwig.actin.PatientRecord
import com.hartwig.actin.algo.datamodel.Evaluation
import com.hartwig.actin.algo.datamodel.EvaluationResult
import com.hartwig.actin.algo.evaluation.EvaluationFactory.unrecoverable
import com.hartwig.actin.algo.evaluation.EvaluationFunction

class HasAvailablePDL1Status internal constructor() : EvaluationFunction {
    override fun evaluate(record: PatientRecord): Evaluation {
        return unrecoverable()
            .result(EvaluationResult.UNDETERMINED)
            .addUndeterminedSpecificMessages("Availability of PD-L1 status currently cannot be determined")
            .addUndeterminedGeneralMessages("PD-L1 status not yet determined")
            .build()
    }
}