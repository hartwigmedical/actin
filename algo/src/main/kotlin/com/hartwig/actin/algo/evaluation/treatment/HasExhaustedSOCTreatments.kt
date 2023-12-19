package com.hartwig.actin.algo.evaluation.treatment

import com.hartwig.actin.PatientRecord
import com.hartwig.actin.algo.datamodel.Evaluation
import com.hartwig.actin.algo.evaluation.EvaluationFactory
import com.hartwig.actin.algo.evaluation.EvaluationFunction

class HasExhaustedSOCTreatments : EvaluationFunction {

    override fun evaluate(record: PatientRecord): Evaluation {
        return if (record.clinical().oncologicalHistory().isEmpty()) {
            EvaluationFactory.undetermined(
                "Patient has not had any prior cancer treatments and therefore undetermined exhaustion of SOC",
                "Undetermined exhaustion of SOC"
            )
        } else {
            EvaluationFactory.notEvaluated(
                "Assumed exhaustion of SOC since patient has had prior cancer treatment",
                "Assumed exhaustion of SOC"
            )
        }
    }
}