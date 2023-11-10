package com.hartwig.actin.algo.evaluation.vitalfunction

import com.hartwig.actin.PatientRecord
import com.hartwig.actin.algo.datamodel.Evaluation
import com.hartwig.actin.algo.evaluation.EvaluationFactory
import com.hartwig.actin.algo.evaluation.EvaluationFunction
import com.hartwig.actin.clinical.sort.BodyWeightDescendingDateComparator

open class BodyWeightFunctions(private val referenceBodyWeight: Double, private val referenceIsMinimum: Boolean) :
    EvaluationFunction {
    override fun evaluate(record: PatientRecord): Evaluation {
        val mostRecent = record.clinical().bodyWeights().sortedWith(BodyWeightDescendingDateComparator()).firstOrNull()
            ?: return EvaluationFactory.undetermined("No body weights found", "No body weights found")

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
                if (referenceIsMinimum) EvaluationFactory.fail(
                    specificMessage,
                    generalMessage
                ) else EvaluationFactory.pass(
                    specificMessage,
                    generalMessage
                )
            }

            else -> {
                val specificMessage = "Patient has body weight equal to or above $referenceBodyWeight"
                val generalMessage = "Body weight equal to or above $referenceBodyWeight"
                if (referenceIsMinimum) EvaluationFactory.pass(
                    specificMessage,
                    generalMessage
                ) else EvaluationFactory.fail(
                    specificMessage,
                    generalMessage
                )
            }
        }
    }

    companion object {
        const val EXPECTED_UNIT: String = "kilogram"
    }

}



