package com.hartwig.actin.algo.evaluation.infection

import com.hartwig.actin.PatientRecord
import com.hartwig.actin.algo.datamodel.Evaluation
import com.hartwig.actin.algo.evaluation.EvaluationFactory
import com.hartwig.actin.algo.evaluation.EvaluationFunction

class CanAdhereToAttenuatedVaccineUse: EvaluationFunction {
    override fun evaluate(record: PatientRecord): Evaluation {
        return EvaluationFactory.notEvaluated(
            "Adherence to protocol for attenuated vaccine use is currently not evaluated",
            "Assumed attenuated vaccine protocol adherence"
        )
    }
}