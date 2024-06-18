package com.hartwig.actin.algo.evaluation.treatment

import com.hartwig.actin.PatientRecord
import com.hartwig.actin.algo.datamodel.Evaluation
import com.hartwig.actin.algo.evaluation.EvaluationFactory
import com.hartwig.actin.algo.evaluation.EvaluationFunction

class HasReceivedPlatinumBasedDoublet : EvaluationFunction {

    override fun evaluate(record: PatientRecord): Evaluation {

        val message = "received platinum based doublet chemotherapy"

        return when {
            TreatmentFunctions.receivedPlatinumDoublet(record) -> {
                EvaluationFactory.pass("Patient has $message", "Has $message ")
            }

            TreatmentFunctions.receivedPlatinumTripletOrAbove(record) -> {
                EvaluationFactory.warn(
                    "Patient has received platinum chemotherapy combination but not in doublet (more than 2 drugs combined)",
                    "Has received platinum chemotherapy combination but not in doublet (more than 2 drugs combined)"
                )
            }

            else -> {
                EvaluationFactory.fail("Patient has not $message", "Has not $message")
            }
        }
    }
}
