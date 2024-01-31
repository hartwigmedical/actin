package com.hartwig.actin.algo.evaluation.molecular

import com.hartwig.actin.PatientRecord
import com.hartwig.actin.algo.datamodel.Evaluation
import com.hartwig.actin.algo.evaluation.EvaluationFactory
import com.hartwig.actin.algo.evaluation.EvaluationFunction

//TODO: Implement according to README
class AnyGeneHasDriverEventWithApprovedTherapy : EvaluationFunction {
    override fun evaluate(record: PatientRecord): Evaluation {
        return EvaluationFactory.undetermined(
            "Driver events in genes with approved therapy are currently not determined",
            "Undetermined if there are driver events with approved therapy"
        )
    }
}