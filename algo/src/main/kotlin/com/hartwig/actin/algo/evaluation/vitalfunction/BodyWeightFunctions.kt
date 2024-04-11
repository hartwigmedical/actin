package com.hartwig.actin.algo.evaluation.vitalfunction

import com.hartwig.actin.PatientRecord
import com.hartwig.actin.algo.datamodel.Evaluation
import com.hartwig.actin.algo.evaluation.EvaluationFactory
import com.hartwig.actin.clinical.datamodel.BodyWeight
import com.hartwig.actin.clinical.sort.BodyWeightDescendingDateComparator
import java.time.LocalDate
import kotlin.math.ceil

object BodyWeightFunctions {

    fun evaluatePatientForMaximumBodyWeight(record: PatientRecord, maxBodyWeight: Double, minimumDate: LocalDate): Evaluation {
        return evaluatePatientBodyWeightAgainstReference(record, maxBodyWeight, false, minimumDate)
        }

    fun evaluatePatientForMinimumBodyWeight(record: PatientRecord, minBodyWeight: Double, minimumDate: LocalDate): Evaluation {
        return evaluatePatientBodyWeightAgainstReference(record, minBodyWeight, true, minimumDate)
    }

    private fun evaluatePatientBodyWeightAgainstReference(
        record: PatientRecord, referenceBodyWeight: Double, referenceIsMinimum: Boolean, minimumDate: LocalDate
    ): Evaluation {
        val relevant = selectMedianBodyWeightPerDay(record, minimumDate)
            ?: return if (record.bodyWeights.isNotEmpty() &&
                record.bodyWeights.none { it.unit.equals(EXPECTED_UNIT, ignoreCase = true) }
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

        val median = determineMedianBodyWeight(relevant)
        val referenceWithMargin = if (referenceIsMinimum) {
            referenceBodyWeight * VitalFunctionRuleMapper.BODY_WEIGHT_NEGATIVE_MARGIN_OF_ERROR
        } else referenceBodyWeight * VitalFunctionRuleMapper.BODY_WEIGHT_POSITIVE_MARGIN_OF_ERROR
        val comparisonWithMargin = median.compareTo(referenceWithMargin)
        val comparisonWithoutMargin = median.compareTo(referenceBodyWeight)

        return when {

            (!referenceIsMinimum && comparisonWithoutMargin > 0 && comparisonWithMargin <= 0)
                    || (referenceIsMinimum && comparisonWithoutMargin < 0 && comparisonWithMargin >= 0) -> {
                val specificMessage = "Patient median body weight ($median kg) is below $referenceBodyWeight kg"
                val generalMessage = "Median body weight ($median kg) below $referenceBodyWeight kg"
                EvaluationFactory.recoverableUndetermined(specificMessage, generalMessage)
            }

            comparisonWithoutMargin < 0 -> {
                val specificMessage = "Patient median body weight ($median kg) is below $referenceBodyWeight kg"
                val generalMessage = "Median body weight ($median kg) below $referenceBodyWeight kg"
                if (referenceIsMinimum) {
                    EvaluationFactory.fail(specificMessage, generalMessage)
                } else {
                    EvaluationFactory.pass(specificMessage, generalMessage)
                }
            }

            comparisonWithoutMargin == 0 -> {
                val specificMessage = "Patient median body weight ($median kg) is equal to $referenceBodyWeight kg"
                val generalMessage = "Median body weight ($median kg) equal to $referenceBodyWeight kg"

                return EvaluationFactory.pass(specificMessage, generalMessage)
            }

            else -> {
                val specificMessage = "Patient median body weight ($median kg) is above $referenceBodyWeight kg"
                val generalMessage = "Median body weight ($median kg) above $referenceBodyWeight kg"
                if (referenceIsMinimum) {
                    EvaluationFactory.pass(specificMessage, generalMessage)
                } else {
                    EvaluationFactory.fail(specificMessage, generalMessage)
                }
            }
        }
    }

    fun selectMedianBodyWeightPerDay(record: PatientRecord, minimalDate: LocalDate): List<BodyWeight>? {
        val result = record.bodyWeights
            .filter { it.date.toLocalDate() > minimalDate && it.valid }
            .groupBy { it.date }
            .map { selectMedianBodyWeightValue(it.value) }
            .sortedWith(BodyWeightDescendingDateComparator())
            .take(MAX_ENTRIES)
        return result.ifEmpty { null }
    }

    private fun selectMedianBodyWeightValue(bodyWeights: List<BodyWeight>): BodyWeight {
        val sorted = bodyWeights.sortedBy(BodyWeight::value)
        return sorted[ceil(sorted.size / 2.0).toInt() - 1]

    }

    private fun sortedBodyWeightValues(bodyWeights: Iterable<BodyWeight>): List<Double> {
        return bodyWeights.map { it.value }.sorted()
    }

    fun determineMedianBodyWeight(bodyWeights: Iterable<BodyWeight>): Double {
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



