package com.hartwig.actin.algo.evaluation.tumor

import com.hartwig.actin.algo.evaluation.EvaluationFactory
import com.hartwig.actin.algo.evaluation.EvaluationFunction
import com.hartwig.actin.datamodel.PatientRecord
import com.hartwig.actin.datamodel.algo.Evaluation

class MeetsSpecificCriteriaRegardingBrainMetastases : EvaluationFunction {

    override fun evaluate(record: PatientRecord): Evaluation {
        with(record.tumor) {
            val messageStart = "Undetermined if study specific criteria regarding"
            val unknownBrainLesions = hasBrainLesions == null

            // We assume that if a patient has active brain metastases, hasBrainMetastases is allowed to be (theoretically) null/false
            return when {
                hasActiveBrainLesions == true -> {
                    EvaluationFactory.undetermined("$messageStart brain metastases are met")
                }

                hasBrainLesions == true -> {
                    EvaluationFactory.undetermined("$messageStart brain metastases are met")
                }

                hasSuspectedBrainLesions == true -> {
                    EvaluationFactory.undetermined("$messageStart suspected brain metastases are met")
                }

                unknownBrainLesions && hasCnsLesions == true -> {
                    EvaluationFactory.undetermined("$messageStart brain metastases are met")
                }

                unknownBrainLesions && hasSuspectedCnsLesions == true -> {
                    EvaluationFactory.undetermined("$messageStart suspected brain metastases are met")
                }

                unknownBrainLesions -> {
                    EvaluationFactory.undetermined("Undetermined if specific criteria regarding brain metastases are met " +
                            "(brain lesions data missing)")
                }

                else -> {
                    EvaluationFactory.fail(
                        "No brain metastases present hence won't meet study specific criteria regarding brain metastases"
                    )
                }
            }
        }
    }
}