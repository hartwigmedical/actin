package com.hartwig.actin.algo.evaluation.tumor

import com.hartwig.actin.algo.evaluation.EvaluationFactory
import com.hartwig.actin.algo.evaluation.EvaluationFunction
import com.hartwig.actin.datamodel.PatientRecord
import com.hartwig.actin.datamodel.algo.Evaluation

class HasKnownBrainMetastases : EvaluationFunction {

    override fun evaluate(record: PatientRecord): Evaluation {
        with(record.tumor) {
            return when {
                hasBrainLesions == true -> {
                    EvaluationFactory.pass("Has brain metastases")
                }

                hasSuspectedBrainLesions == true -> {
                    val message = "Brain metastases present but suspected lesions only"
                    EvaluationFactory.warn(message)
                }

                hasBrainLesions == null -> {
                    val message = "Undetermined if brain metastases present (brain lesions data missing)"
                    EvaluationFactory.undetermined(message)
                }

                else -> EvaluationFactory.fail("No known brain metastases present")
            }
        }
    }
}