package com.hartwig.actin.algo.evaluation.othercondition

import com.hartwig.actin.algo.evaluation.EvaluationFactory
import com.hartwig.actin.algo.evaluation.EvaluationFunction
import com.hartwig.actin.datamodel.PatientRecord
import com.hartwig.actin.datamodel.algo.Evaluation

class HasPotentialContraIndicationForStereotacticRadiosurgery : EvaluationFunction {

    override fun evaluate(record: PatientRecord): Evaluation {
        return EvaluationFactory.recoverableUndetermined(
            "Undetermined if patient has a potential contra-indication for stereotactic radiosurgery - assumed there is none",
            "Undetermined contra-indication for stereotactic radiosurgery - assumed there is none"
        )
    }
}