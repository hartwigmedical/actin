package com.hartwig.actin.algo.evaluation.molecular

import com.hartwig.actin.PatientRecord
import com.hartwig.actin.algo.datamodel.Evaluation
import com.hartwig.actin.algo.datamodel.EvaluationResult
import com.hartwig.actin.algo.evaluation.EvaluationFactory.unrecoverable
import com.hartwig.actin.algo.evaluation.EvaluationFunction

//TODO: Implement according to README
class AnyGeneHasDriverEventWithApprovedTherapy internal constructor() : EvaluationFunction {
    override fun evaluate(record: PatientRecord): Evaluation {
        return unrecoverable()
            .result(EvaluationResult.UNDETERMINED)
            .addUndeterminedSpecificMessages("Driver events in genes with approved therapy are currently not determined")
            .addUndeterminedGeneralMessages("Undetermined if there are driver events with approved therapy")
            .build()
    }
}