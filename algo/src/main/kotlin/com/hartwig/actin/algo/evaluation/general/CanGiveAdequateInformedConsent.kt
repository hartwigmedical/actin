package com.hartwig.actin.algo.evaluation.general

import com.hartwig.actin.algo.evaluation.EvaluationFactory
import com.hartwig.actin.algo.evaluation.EvaluationFunction
import com.hartwig.actin.datamodel.PatientRecord
import com.hartwig.actin.datamodel.algo.Evaluation

class CanGiveAdequateInformedConsent : EvaluationFunction {
    
    override fun evaluate(record: PatientRecord): Evaluation {
        return EvaluationFactory.notEvaluated(
            "Currently assumed that adequate informed consent can/will be given", "Adequate informed consent"
        )
    }
}