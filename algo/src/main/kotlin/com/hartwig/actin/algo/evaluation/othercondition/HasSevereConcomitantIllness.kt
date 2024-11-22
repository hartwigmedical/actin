package com.hartwig.actin.algo.evaluation.othercondition

import com.hartwig.actin.algo.evaluation.EvaluationFactory
import com.hartwig.actin.algo.evaluation.EvaluationFunction
import com.hartwig.actin.datamodel.PatientRecord
import com.hartwig.actin.datamodel.algo.Evaluation

class HasSevereConcomitantIllness: EvaluationFunction {

    override fun evaluate(record: PatientRecord): Evaluation {
        val whoStatus = record.clinicalStatus.who

        if (whoStatus == 3 || whoStatus == 4) {
            return EvaluationFactory.warn(
                "Patient may have severe concomitant illnesses based on WHO status of $whoStatus",
                "Potential severe concomitant illnesses due to WHO $whoStatus"
            )
        }
        return if (whoStatus == 5) {
            EvaluationFactory.pass(
                "WHO status of patient is WHO 5",
                "Severe concomitant illnesses due to WHO 5"
            )
        } else
            EvaluationFactory.notEvaluated(
                "Severe concomitant illnesses are assumed not to be present",
                "Assumed no severe concomitant illnesses"
            )
    }
}