package com.hartwig.actin.algo.evaluation.treatment

import com.hartwig.actin.PatientRecord
import com.hartwig.actin.algo.datamodel.Evaluation
import com.hartwig.actin.algo.evaluation.EvaluationFactory
import com.hartwig.actin.algo.evaluation.EvaluationFunction

//TODO: Implement according to README
class HasHadLocalHepaticTherapyWithinWeeks internal constructor() : EvaluationFunction {
    override fun evaluate(record: PatientRecord): Evaluation {
        return EvaluationFactory.undetermined(
            "Not determined yet if patient has had local hepatic therapy within certain weeks",
            "Undetermined if patient has had local hepatic therapy"
        )
    }
}