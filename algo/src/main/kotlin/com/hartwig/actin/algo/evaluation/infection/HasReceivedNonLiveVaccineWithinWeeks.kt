package com.hartwig.actin.algo.evaluation.infection

import com.hartwig.actin.PatientRecord
import com.hartwig.actin.algo.datamodel.Evaluation
import com.hartwig.actin.algo.evaluation.EvaluationFactory
import com.hartwig.actin.algo.evaluation.EvaluationFunction

class HasReceivedNonLiveVaccineWithinWeeks internal constructor() : EvaluationFunction {
    override fun evaluate(record: PatientRecord): Evaluation {
        return EvaluationFactory.undetermined(
            "Unknown if patient has received a non-live vaccine within nr of weeks",
            "Unknown non-live vaccine status"
        )
    }
}