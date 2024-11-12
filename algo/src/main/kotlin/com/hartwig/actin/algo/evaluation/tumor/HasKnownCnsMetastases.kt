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
                    EvaluationFactory.pass("CNS metastases are present", "CNS metastases")
                }

                hasBrainLesions == true -> {
                    EvaluationFactory.pass("Brain metastases are present", "Brain metastases")
                }

                hasSuspectedCnsLesions == true || hasSuspectedBrainLesions == true -> {
                    val message = "CNS metastases present but suspected lesions only"
                    EvaluationFactory.warn(message, message)
                }

                hasCnsLesions == null || hasBrainLesions == null -> {
                    val message = "Undetermined if CNS metastases present (data missing)"
                    EvaluationFactory.recoverableUndetermined(message, message)
                }

                else -> EvaluationFactory.fail("No known CNS metastases present", "No known CNS metastases")
            }
        }
    }
}