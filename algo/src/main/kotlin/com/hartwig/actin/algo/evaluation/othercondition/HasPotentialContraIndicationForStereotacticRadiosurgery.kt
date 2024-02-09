package com.hartwig.actin.algo.evaluation.othercondition

import com.hartwig.actin.PatientRecord
import com.hartwig.actin.algo.datamodel.Evaluation
import com.hartwig.actin.algo.evaluation.EvaluationFactory
import com.hartwig.actin.algo.evaluation.EvaluationFunction

class HasPotentialContraIndicationForStereotacticRadiosurgery : EvaluationFunction {

    override fun evaluate(record: PatientRecord): Evaluation {
        return EvaluationFactory.notEvaluated(
            "Currently not evaluated if patient has a potential contra-indication for stereotactic radiosurgery - assumed there is none",
            "Assumed there is no contra-indication for stereotactic radiosurgery (not evaluated)"
        )
    }
}