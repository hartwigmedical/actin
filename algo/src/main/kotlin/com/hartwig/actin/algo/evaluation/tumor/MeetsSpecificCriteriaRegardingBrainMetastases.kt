package com.hartwig.actin.algo.evaluation.tumor

import com.hartwig.actin.algo.evaluation.EvaluationFactory
import com.hartwig.actin.algo.evaluation.EvaluationFunction
import com.hartwig.actin.datamodel.PatientRecord
import com.hartwig.actin.datamodel.algo.Evaluation

class MeetsSpecificCriteriaRegardingBrainMetastases : EvaluationFunction {

    override fun evaluate(record: PatientRecord): Evaluation {
        val tumorDetails = record.tumor

        val (hasBrainMetastases, hasActiveBrainMetastases, hasSuspectedBrainMetastases) = listOf(
            tumorDetails.hasBrainLesions,
            tumorDetails.hasActiveBrainLesions,
            tumorDetails.hasSuspectedBrainLesions
        )

        val (hasCNSMetastases, hasSuspectedCnsMetastases) = listOf(
            tumorDetails.hasCnsLesions,
            tumorDetails.hasSuspectedCnsLesions
        )

        val specificMessageEnding = "if these meet the specific protocol criteria"
        val generalMessageStart = "Undetermined if study specific criteria regarding"

        // We assume that if a patient has active brain metastases, hasBrainMetastases is allowed to be (theoretically) null/false
        return when {
            hasActiveBrainMetastases == true -> {
                EvaluationFactory.undetermined(
                    "Patient has brain metastases that are considered active, undetermined $specificMessageEnding",
                    "$generalMessageStart brain metastases are met"
                )
            }

            hasBrainMetastases == true -> {
                EvaluationFactory.undetermined(
                    "Patient has brain metastases, undetermined $specificMessageEnding",
                    "$generalMessageStart brain metastases are met"
                )
            }

            hasSuspectedBrainMetastases == true -> {
                EvaluationFactory.undetermined(
                    "Patient has suspected brain metastases, undetermined $specificMessageEnding",
                    "$generalMessageStart suspected brain metastases are met"
                )
            }

            hasBrainMetastases == null -> {
                val specificEnding = "CNS metastases, undetermined if patient also has brain metastases and $specificMessageEnding"
                val generalStart = "Undetermined if study specific criteria regarding"

                when {
                    hasCNSMetastases == true -> {
                        EvaluationFactory.undetermined("Patient has $specificEnding", "$generalStart brain metastases are met")
                    }

                    hasSuspectedCnsMetastases == true -> {
                        EvaluationFactory.undetermined(
                            "Patient has suspected $specificEnding",
                            "$generalStart suspected brain metastases are met"
                        )
                    }

                    else -> {
                        val message = "$generalMessageStart brain metastases are met (data missing)"
                        EvaluationFactory.recoverableUndetermined(message, message)
                    }
                }
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