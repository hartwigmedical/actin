package com.hartwig.actin.algo.evaluation.tumor

import com.hartwig.actin.algo.evaluation.EvaluationFactory
import com.hartwig.actin.algo.evaluation.EvaluationFunction
import com.hartwig.actin.datamodel.PatientRecord
import com.hartwig.actin.datamodel.algo.Evaluation

class MeetsSpecificCriteriaRegardingBrainMetastases : EvaluationFunction {

    override fun evaluate(record: PatientRecord): Evaluation {

        with(record.tumor) {
            val specificMessageEnding = "if these meet the specific protocol criteria"
            val generalMessageStart = "Undetermined if study specific criteria regarding"
            val unknownBrainLesions = hasBrainLesions == null
            val unknownBrainMetastasesMessageEnding =
                "CNS metastases, undetermined if patient also has brain metastases and $specificMessageEnding"
            val unknownBrainMetastasesMessageStart = "Undetermined if study specific criteria regarding"

            // We assume that if a patient has active brain metastases, hasBrainMetastases is allowed to be (theoretically) null/false
            return when {
                hasActiveBrainLesions == true -> {
                    EvaluationFactory.undetermined(
                        "Patient has brain metastases that are considered active, undetermined $specificMessageEnding",
                        "$generalMessageStart brain metastases are met"
                    )
                }

                hasBrainLesions == true -> {
                    EvaluationFactory.undetermined(
                        "Patient has brain metastases, undetermined $specificMessageEnding",
                        "$generalMessageStart brain metastases are met"
                    )
                }

                hasSuspectedBrainLesions == true -> {
                    EvaluationFactory.undetermined(
                        "Patient has suspected brain metastases, undetermined $specificMessageEnding",
                        "$generalMessageStart suspected brain metastases are met"
                    )
                }

                unknownBrainLesions && hasCnsLesions == true -> {
                    EvaluationFactory.undetermined(
                        "Patient has $unknownBrainMetastasesMessageEnding",
                        "$unknownBrainMetastasesMessageStart brain metastases are met"
                    )
                }

                unknownBrainLesions && hasSuspectedCnsLesions == true -> {
                    EvaluationFactory.undetermined(
                        "Patient has suspected $unknownBrainMetastasesMessageEnding",
                        "$unknownBrainMetastasesMessageStart suspected brain metastases are met"
                    )
                }

                unknownBrainLesions -> {
                    val message = "Undetermined if specific criteria regarding brain metastases are met (data missing)"
                    EvaluationFactory.undetermined(message, message)
                }

                else -> {
                    EvaluationFactory.fail(
                        "No known brain metastases present hence also won't meet specific protocol criteria regarding brain metastases",
                        "No known brain metastases"
                    )
                }
            }
        }
    }
}