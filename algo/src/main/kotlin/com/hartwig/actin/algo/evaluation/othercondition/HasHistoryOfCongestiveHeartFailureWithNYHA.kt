package com.hartwig.actin.algo.evaluation.othercondition

import com.hartwig.actin.PatientRecord
import com.hartwig.actin.algo.datamodel.Evaluation
import com.hartwig.actin.algo.evaluation.EvaluationFactory
import com.hartwig.actin.algo.evaluation.EvaluationFunction

class HasHistoryOfCongestiveHeartFailureWithNYHA internal constructor() : EvaluationFunction {
    override fun evaluate(record: PatientRecord): Evaluation {
        return EvaluationFactory.undetermined(
            "Currently undetermined if patient has history of congestive heart failure with NYHA class",
            "Undetermined congestive heart failure"
        )
    }
}