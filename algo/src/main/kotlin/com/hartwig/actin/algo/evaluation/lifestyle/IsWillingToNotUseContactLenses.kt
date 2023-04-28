package com.hartwig.actin.algo.evaluation.lifestyle

import com.hartwig.actin.PatientRecord
import com.hartwig.actin.algo.datamodel.Evaluation
import com.hartwig.actin.algo.evaluation.EvaluationFactory
import com.hartwig.actin.algo.evaluation.EvaluationFunction

class IsWillingToNotUseContactLenses internal constructor() : EvaluationFunction {
    override fun evaluate(record: PatientRecord): Evaluation {
        return EvaluationFactory.warn(
            "Cannot be evaluated if patient is willing/able not to use contact lenses",
            "Potential willingness/ability not to use contact lenses unknown"
        )
    }
}