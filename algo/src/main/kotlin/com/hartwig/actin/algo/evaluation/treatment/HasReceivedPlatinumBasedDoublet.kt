package com.hartwig.actin.algo.evaluation.treatment

import com.hartwig.actin.algo.evaluation.EvaluationFactory
import com.hartwig.actin.algo.evaluation.EvaluationFunction
import com.hartwig.actin.datamodel.PatientRecord
import com.hartwig.actin.datamodel.algo.Evaluation

class HasReceivedPlatinumBasedDoublet : EvaluationFunction {

    override fun evaluate(record: PatientRecord): Evaluation {
        val message = "received platinum based doublet chemotherapy"

        return when {
            TreatmentFunctions.receivedPlatinumDoublet(record) -> {
                EvaluationFactory.pass("Has $message ")
            }

            TreatmentFunctions.receivedPlatinumTripletOrAbove(record) -> {
                EvaluationFactory.warn("Has received platinum chemotherapy combination but not in doublet (more than 2 drugs combined)")
            }

            else -> {
                EvaluationFactory.fail("Has not $message")
            }
        }
    }
}
