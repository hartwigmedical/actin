package com.hartwig.actin.algo.evaluation.reproduction

import com.hartwig.actin.PatientRecord
import com.hartwig.actin.algo.datamodel.Evaluation
import com.hartwig.actin.algo.evaluation.EvaluationFactory
import com.hartwig.actin.algo.evaluation.EvaluationFunction

class CanUseAdequateAnticonception internal constructor() : EvaluationFunction {
    override fun evaluate(record: PatientRecord): Evaluation {
        return EvaluationFactory.undetermined(
            "Assumed that patient will adhere to relevant anticonception prescriptions",
            "Assumed adherence to anticonception prescriptions"
        )
    }
}