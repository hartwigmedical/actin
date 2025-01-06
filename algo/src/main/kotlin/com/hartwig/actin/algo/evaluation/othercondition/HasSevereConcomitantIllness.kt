package com.hartwig.actin.algo.evaluation.othercondition

import com.hartwig.actin.algo.evaluation.EvaluationFactory
import com.hartwig.actin.algo.evaluation.EvaluationFunction
import com.hartwig.actin.datamodel.PatientRecord
import com.hartwig.actin.datamodel.algo.Evaluation

class HasSevereConcomitantIllness: EvaluationFunction {

    override fun evaluate(record: PatientRecord): Evaluation {
        val whoStatus = record.clinicalStatus.who

        if (whoStatus == 3 || whoStatus == 4) {
            return EvaluationFactory.warn("Potential severe concomitant illnesses (WHO $whoStatus)")
        }
        return if (whoStatus == 5) {
            EvaluationFactory.pass("Has severe concomitant illness (WHO 5)")
        } else
            EvaluationFactory.notEvaluated("Assumed that severe concomitant illnesses are not to be present")
    }
}