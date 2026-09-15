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
                    EvaluationFactory.pass("Brain metastases in provided lesions")
                }

                hasSuspectedBrainLesions == true -> {
                    val message = "Brain metastases in provided lesions but suspected lesions only"
                    EvaluationFactory.warn(message)
                }

                hasBrainLesions == null -> {
                    val message = "Undetermined if brain metastases based on provided lesions"
                    EvaluationFactory.undetermined(message)
                }

                else -> EvaluationFactory.fail("No known brain metastases in provided lesions")
            }
        }
    }
}