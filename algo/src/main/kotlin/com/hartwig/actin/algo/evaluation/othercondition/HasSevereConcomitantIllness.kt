package com.hartwig.actin.algo.evaluation.othercondition

import com.hartwig.actin.PatientRecord
import com.hartwig.actin.algo.datamodel.Evaluation
import com.hartwig.actin.algo.evaluation.EvaluationFactory
import com.hartwig.actin.algo.evaluation.EvaluationFunction

class HasSevereConcomitantIllness internal constructor() : EvaluationFunction {
    override fun evaluate(record: PatientRecord): Evaluation {
        val whoStatus = record.clinical().clinicalStatus().who()
            ?: return EvaluationFactory.undetermined(
                "Undetermined whether patient may have severe concomitant illnesses (who status unknown)",
                "Undetermined severe concomitant illnesses"
            )
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