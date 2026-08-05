package com.hartwig.actin.algo.evaluation.vitalfunction

import com.hartwig.actin.algo.evaluation.EvaluationFactory
import com.hartwig.actin.algo.evaluation.EvaluationLabels
import com.hartwig.actin.clinical.sort.BodyWeightDescendingDateComparator
import com.hartwig.actin.datamodel.PatientRecord
import com.hartwig.actin.datamodel.algo.Evaluation
import com.hartwig.actin.datamodel.clinical.BodyWeight
import java.time.LocalDate
import kotlin.math.ceil

object BodyWeightFunctions {

    fun evaluatePatientForMaximumBodyWeight(
        record: PatientRecord, maxBodyWeight: Double, minimumDate: LocalDate, labels: EvaluationLabels.VitalFunction
    ): Evaluation {
        return evaluatePatientBodyWeightAgainstReference(record, maxBodyWeight, false, minimumDate, labels)
        }

    fun evaluatePatientForMinimumBodyWeight(
        record: PatientRecord, minBodyWeight: Double, minimumDate: LocalDate, labels: EvaluationLabels.VitalFunction
    ): Evaluation {
        return evaluatePatientBodyWeightAgainstReference(record, minBodyWeight, true, minimumDate, labels)
    }

    private fun evaluatePatientBodyWeightAgainstReference(
        record: PatientRecord,
        referenceBodyWeight: Double,
        referenceIsMinimum: Boolean,
        minimumDate: LocalDate,
        labels: EvaluationLabels.VitalFunction
    ): Evaluation {
        val relevant = selectMedianBodyWeightPerDay(record, minimumDate)
            ?: return if (record.bodyWeights.isNotEmpty() &&
                record.bodyWeights.none { weight -> EXPECTED_UNITS.any { it.equals(weight.unit, ignoreCase = true) } }
            ) {
                EvaluationFactory.undetermined(labels.bodyWeightFunctionsWrongUnit(EXPECTED_UNITS.joinToString(" or ")))
            } else {
                EvaluationFactory.recoverableUndetermined(labels.bodyWeightFunctionsNoData())
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
                EvaluationFactory.recoverableUndetermined(labels.bodyWeightFunctionsBelowReference(median, referenceBodyWeight))
            }

            comparisonWithoutMargin < 0 -> {
                val message = labels.bodyWeightFunctionsBelowReference(median, referenceBodyWeight)
                if (referenceIsMinimum) {
                    EvaluationFactory.recoverableFail(message)
                } else {
                    EvaluationFactory.recoverablePass(message)
                }
            }

            comparisonWithoutMargin == 0 -> {
                EvaluationFactory.recoverablePass(labels.bodyWeightFunctionsEqualToReference(median, referenceBodyWeight))
            }

            else -> {
                val message = labels.bodyWeightFunctionsAboveReference(median, referenceBodyWeight)
                if (referenceIsMinimum) {
                    EvaluationFactory.recoverablePass(message)
                } else {
                    EvaluationFactory.recoverableFail(message)
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



