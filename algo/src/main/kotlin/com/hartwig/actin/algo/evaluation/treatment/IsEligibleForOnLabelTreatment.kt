package com.hartwig.actin.algo.evaluation.treatment

import com.hartwig.actin.PatientRecord
import com.hartwig.actin.algo.datamodel.Evaluation
import com.hartwig.actin.algo.evaluation.EvaluationFactory
import com.hartwig.actin.algo.evaluation.EvaluationFunction

class IsEligibleForOnLabelTreatment : EvaluationFunction {

    override fun evaluate(record: PatientRecord): Evaluation {
        return EvaluationFactory.undetermined(
            "Potential eligibility for on-label treatment undetermined",
            "Adequate exhaustion of SOC currently undetermined"
        )
    }
}