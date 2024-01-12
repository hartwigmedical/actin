package com.hartwig.actin.algo.evaluation.vitalfunction

import com.hartwig.actin.PatientRecord
import com.hartwig.actin.algo.datamodel.Evaluation
import com.hartwig.actin.algo.evaluation.EvaluationFactory
import com.hartwig.actin.clinical.sort.BodyWeightDescendingDateComparator

object BodyWeightFunctions {

    fun evaluatePatientForMaximumBodyWeight(record: PatientRecord, maxBodyWeight: Double): Evaluation {
        return evaluatePatientBodyWeightAgainstReference(record, maxBodyWeight, false)
        }

    fun evaluatePatientForMinimumBodyWeight(record: PatientRecord, minBodyWeight: Double): Evaluation {
        return evaluatePatientBodyWeightAgainstReference(record, minBodyWeight, true)
    }

    private fun evaluatePatientBodyWeightAgainstReference(
        record: PatientRecord, referenceBodyWeight: Double, referenceIsMinimum: Boolean
    ): Evaluation {
        val mostRecent = record.clinical.bodyWeights.sortedWith(BodyWeightDescendingDateComparator()).firstOrNull()
            ?: return EvaluationFactory.undetermined(
                "No body weights found", "No body weights found"
            )

        if (!mostRecent.unit.equals(EXPECTED_UNIT, ignoreCase = true)) {
            return EvaluationFactory.undetermined(
                "Most recent body weight not measured in $EXPECTED_UNIT",
                "Invalid body weight unit"
            )
        }

        val comparison = mostRecent.value.compareTo(referenceBodyWeight)

        return when {

            comparison < 0 -> {
                val specificMessage = "Patient body weight ({$mostRecent kg}) is below $referenceBodyWeight kg"
                val generalMessage = "Body weight ({$mostRecent kg}) below $referenceBodyWeight kg"
                if (referenceIsMinimum) {
                    EvaluationFactory.fail(specificMessage, generalMessage)
                } else {
                    EvaluationFactory.pass(specificMessage, generalMessage)
                }
            }

            comparison == 0 -> {
                val specificMessage = "Patient body weight ({$mostRecent kg}) is equal to $referenceBodyWeight kg"
                val generalMessage = "Body weight ({$mostRecent kg}) equal to $referenceBodyWeight kg"

                return EvaluationFactory.pass(specificMessage, generalMessage)
            }

            else -> {
                val specificMessage = "Patient body weight ({$mostRecent kg}) is above $referenceBodyWeight kg"
                val generalMessage = "Body weight ({$mostRecent kg}) above $referenceBodyWeight kg"
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



