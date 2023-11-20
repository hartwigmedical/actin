package com.hartwig.actin.algo.evaluation.vitalfunction

import com.hartwig.actin.PatientRecord
import com.hartwig.actin.algo.datamodel.Evaluation
import com.hartwig.actin.algo.evaluation.EvaluationFactory
import com.hartwig.actin.clinical.sort.BodyWeightDescendingDateComparator

object BodyWeightFunctions {

    fun evaluatePatientBodyWeightAgainstReference(
        record: PatientRecord, referenceBodyWeight: Double, referenceIsMinimum: Boolean
    ): Evaluation {
        val mostRecent = record.clinical().bodyWeights().sortedWith(BodyWeightDescendingDateComparator()).firstOrNull()
            ?: return EvaluationFactory.undetermined(
                "No body weights found", "No body weights found"
            )
        val comparison = mostRecent.value().compareTo(referenceBodyWeight)

        return when {
            !mostRecent.unit().equals(EXPECTED_UNIT, ignoreCase = true) -> {
                EvaluationFactory.undetermined(
                    "Most recent body weight not measured in $EXPECTED_UNIT",
                    "Invalid body weight unit"
                )
            }

            comparison < 0 -> {
                val specificMessage = "Patient has body weight below $referenceBodyWeight"
                val generalMessage = "Body weight below $referenceBodyWeight"
                if (referenceIsMinimum) {
                    EvaluationFactory.fail(specificMessage, generalMessage)
                } else {
                    EvaluationFactory.pass(specificMessage, generalMessage)
                }
            }

            comparison == 0 -> {
                val specificMessage = "Patient has body weight equal to $referenceBodyWeight"
                val generalMessage = "Body weight equal to $referenceBodyWeight"

                return EvaluationFactory.pass(specificMessage, generalMessage)
            }

            else -> {
                val specificMessage = "Patient has body weight above $referenceBodyWeight"
                val generalMessage = "Body weight above $referenceBodyWeight"
                if (referenceIsMinimum) {
                    EvaluationFactory.pass(specificMessage, generalMessage)
                } else {
                    EvaluationFactory.fail(specificMessage, generalMessage)
                }
            }
        }
    }

    const val EXPECTED_UNIT = "kilogram"
}



