package com.hartwig.actin.algo.evaluation.vitalfunction

import com.hartwig.actin.PatientRecord
import com.hartwig.actin.algo.datamodel.Evaluation
import com.hartwig.actin.algo.evaluation.EvaluationFactory
import com.hartwig.actin.clinical.datamodel.BodyWeight
import com.hartwig.actin.clinical.sort.BodyWeightDescendingDateComparator
import kotlin.math.ceil

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
        val relevant = selectMedianBodyWeightPerDay(record)
        if (relevant.isEmpty()) {
            return if (record.clinical().bodyWeights().isNotEmpty() &&
                record.clinical().bodyWeights().none { it.unit().equals(EXPECTED_UNIT, ignoreCase = true) }
            ) {
                EvaluationFactory.undetermined(
                    "Body weights not measured in $EXPECTED_UNIT",
                    "Invalid body weight unit"
                )
            } else {
                EvaluationFactory.recoverableUndetermined(
                    "No (recent) body weights found", "No (recent) body weights found"
                )
            }
        }

        val median = determineMedianBodyWeight(relevant)
        val comparison = median.compareTo(referenceBodyWeight)

        return when {

            comparison < 0 -> {
                val specificMessage = "Patient median body weight ({$median kg}) is below $referenceBodyWeight kg"
                val generalMessage = "Median body weight ({$median kg}) below $referenceBodyWeight kg"
                if (referenceIsMinimum) {
                    EvaluationFactory.fail(specificMessage, generalMessage)
                } else {
                    EvaluationFactory.pass(specificMessage, generalMessage)
                }
            }

            comparison == 0 -> {
                val specificMessage = "Patient median body weight ({$median kg}) is equal to $referenceBodyWeight kg"
                val generalMessage = "Median body weight ({$median kg}) equal to $referenceBodyWeight kg"

                return EvaluationFactory.pass(specificMessage, generalMessage)
            }

            else -> {
                val specificMessage = "Patient median body weight ({$median kg}) is above $referenceBodyWeight kg"
                val generalMessage = "Median body weight ({$median kg}) above $referenceBodyWeight kg"
                if (referenceIsMinimum) {
                    EvaluationFactory.pass(specificMessage, generalMessage)
                } else {
                    EvaluationFactory.fail(specificMessage, generalMessage)
                }
            }
        }
    }

    private fun selectMedianBodyWeightPerDay(record: PatientRecord): List<BodyWeight> {
        return record.clinical().bodyWeights()
            .filter { it.unit().equals(EXPECTED_UNIT, ignoreCase = true) }
            .groupBy { it.date() }
            .map { selectMedianBodyWeightValue(it.value) }
            .sortedWith(BodyWeightDescendingDateComparator())
            .take(MAX_ENTRIES)
            .toList()
    }

    private fun selectMedianBodyWeightValue(bodyWeights: List<BodyWeight>): BodyWeight {
        val sorted = bodyWeights.sortedBy(BodyWeight::value)
        return sorted[ceil(sorted.size / 2.0).toInt() - 1]

    }

    private fun sortedBodyWeightValues(bodyWeights: Iterable<BodyWeight>): List<Double> {
        return bodyWeights.map { it.value() }.sorted()
    }

    private fun determineMedianBodyWeight(bodyWeights: Iterable<BodyWeight>): Double {
        val values = sortedBodyWeightValues(bodyWeights)
        val index = ceil(values.size / 2.0).toInt() - 1
        return if (values.size % 2 == 0) {
            0.5 * (values[index] + values[index + 1])
        } else {
            values[index]
        }
    }

    const val EXPECTED_UNIT = "kilogram"
    private const val MAX_ENTRIES = 5
}



