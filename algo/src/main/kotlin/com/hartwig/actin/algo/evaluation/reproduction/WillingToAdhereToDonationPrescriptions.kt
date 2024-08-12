package com.hartwig.actin.algo.evaluation.reproduction

import com.hartwig.actin.PatientRecord
import com.hartwig.actin.algo.datamodel.Evaluation
import com.hartwig.actin.algo.evaluation.EvaluationFactory
import com.hartwig.actin.algo.evaluation.EvaluationFunction

class WillingToAdhereToDonationPrescriptions: EvaluationFunction {
    override fun evaluate(record: PatientRecord): Evaluation {
        return EvaluationFactory.notEvaluated(
            "Assumed that patient will adhere to relevant sperm/egg donation prescriptions",
            "Assumed adherence to relevant sperm/egg donation prescriptions"
        )
    }
}