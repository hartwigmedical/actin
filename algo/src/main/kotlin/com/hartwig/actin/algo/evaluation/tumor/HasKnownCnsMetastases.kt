package com.hartwig.actin.algo.evaluation.tumor

import com.hartwig.actin.algo.evaluation.EvaluationFactory
import com.hartwig.actin.algo.evaluation.EvaluationFunction
import com.hartwig.actin.datamodel.PatientRecord
import com.hartwig.actin.datamodel.algo.Evaluation

class HasKnownCnsMetastases : EvaluationFunction {

    override fun evaluate(record: PatientRecord): Evaluation {
        with(record.tumor) {

            return when {
                hasCnsLesions == true -> {
                    EvaluationFactory.pass("CNS metastases in provided lesions")
                }

                hasBrainLesions == true -> {
                    EvaluationFactory.pass("Brain metastases in provided lesions")
                }

                hasSuspectedCnsLesions == true || hasSuspectedBrainLesions == true -> {
                    val message = "CNS metastases in provided lesions but suspected lesions only"
                    EvaluationFactory.warn(message)
                }

                hasCnsLesions == null || hasBrainLesions == null -> {
                    val message = "Undetermined if CNS metastases based on provided lesions"
                    EvaluationFactory.undetermined(message)
                }

                else -> EvaluationFactory.fail("No known CNS metastases in provided lesions")
            }
        }
    }
}