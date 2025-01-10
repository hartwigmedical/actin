package com.hartwig.actin.algo.evaluation.vitalfunction

import com.hartwig.actin.algo.evaluation.EvaluationFactory
import com.hartwig.actin.clinical.sort.BodyWeightDescendingDateComparator
import com.hartwig.actin.datamodel.PatientRecord
import com.hartwig.actin.datamodel.algo.Evaluation
import com.hartwig.actin.datamodel.clinical.BodyWeight
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
                record.bodyWeights.none { weight -> EXPECTED_UNITS.any { it.equals(weight.unit, ignoreCase = true) } }
            ) {
                EvaluationFactory.undetermined(
                    "Body weights not measured in ${EXPECTED_UNITS.joinToString(" or ")}"
                )
            } else {
                EvaluationFactory.recoverableUndetermined(
                    "No (recent) body weights found"
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
                EvaluationFactory.recoverableUndetermined("Median body weight ($median kg) below $referenceBodyWeight kg")
            }

            comparisonWithoutMargin < 0 -> {
                val message = "Median body weight ($median kg) below $referenceBodyWeight kg"
                if (referenceIsMinimum) {
                    EvaluationFactory.fail(message)
                } else {
                    EvaluationFactory.pass(message)
                }
            }

            comparisonWithoutMargin == 0 -> {
                return EvaluationFactory.pass("Median body weight ($median kg) equal to $referenceBodyWeight kg")
            }

            else -> {
                val message = "Median body weight ($median kg) above $referenceBodyWeight kg"
                if (referenceIsMinimum) {
                    EvaluationFactory.pass(message)
                } else {
                    EvaluationFactory.fail(message)
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

    val EXPECTED_UNITS = listOf("kilogram", "kilograms")
    private const val MAX_ENTRIES = 5
}



