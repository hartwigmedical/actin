package com.hartwig.actin.algo.evaluation.treatment

import com.hartwig.actin.PatientRecord
import com.hartwig.actin.algo.datamodel.Evaluation
import com.hartwig.actin.algo.evaluation.EvaluationFactory
import com.hartwig.actin.algo.evaluation.EvaluationFunction

class IsEligibleForOnLabelTreatment : EvaluationFunction {

    override fun evaluate(record: PatientRecord): Evaluation {
        return if (record.clinical().treatmentHistory().isEmpty()) {
            EvaluationFactory.undetermined(
                "Patient has not had any prior cancer treatments and therefore undetermined eligibility for on-label treatment",
                "Undetermined eligibility for on-label treatment"
            )
        } else {
            EvaluationFactory.notEvaluated(
                "Assumed no eligibility for on-label treatment since patient has had prior cancer treatment",
                "Assumed no eligibility for on-label treatment"
            )
        }
    }
}