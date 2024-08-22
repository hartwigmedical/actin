package com.hartwig.actin.algo.evaluation.infection

import com.hartwig.actin.PatientRecord
import com.hartwig.actin.algo.datamodel.Evaluation
import com.hartwig.actin.algo.evaluation.EvaluationFactory
import com.hartwig.actin.algo.evaluation.EvaluationFunction

class MeetsCovid19InfectionRequirements: EvaluationFunction {
    override fun evaluate(record: PatientRecord): Evaluation {
        return EvaluationFactory.recoverableUndetermined(
            "Currently COVID-19 infection status cannot be determined",
            "COVID-19 infection status unknown"
        )
    }
}