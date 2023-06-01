package com.hartwig.actin.algo.evaluation.treatment

import com.hartwig.actin.PatientRecord
import com.hartwig.actin.algo.datamodel.Evaluation
import com.hartwig.actin.algo.evaluation.EvaluationFactory
import com.hartwig.actin.algo.evaluation.EvaluationFunction

class IsEligibleForPalliativeRadiotherapy : EvaluationFunction {
    override fun evaluate(record: PatientRecord): Evaluation {
        val message = "Potential eligibility for palliative radiotherapy undetermined"
        return EvaluationFactory.undetermined(message, message)
    }
}