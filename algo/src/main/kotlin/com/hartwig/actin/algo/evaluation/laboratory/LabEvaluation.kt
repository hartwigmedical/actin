package com.hartwig.actin.algo.evaluation.laboratory

import com.hartwig.actin.algo.evaluation.EvaluationFactory
import com.hartwig.actin.algo.evaluation.util.ValueComparison
import com.hartwig.actin.datamodel.algo.Evaluation
import com.hartwig.actin.datamodel.algo.EvaluationResult
import com.hartwig.actin.datamodel.clinical.LabMeasurement
import com.hartwig.actin.datamodel.clinical.LabValue
import java.time.LocalDate

internal object LabEvaluation {

    val REF_LIMIT_UP_OVERRIDES = mapOf(LabMeasurement.INTERNATIONAL_NORMALIZED_RATIO to 1.1)

    const val LAB_VALUE_NEGATIVE_MARGIN_OF_ERROR = 0.95
    const val LAB_VALUE_POSITIVE_MARGIN_OF_ERROR = 1.05

    fun evaluateVersusMinULN(labValue: LabValue, minULNFactor: Double): LabEvaluationResult {
        val refLimitUp = retrieveRefLimitUp(labValue) ?: return LabEvaluationResult.CANNOT_BE_DETERMINED
        val minValue = refLimitUp * minULNFactor
        return evaluateVersusMinValueWithMargin(labValue.value, labValue.comparator, minValue)
    }

    fun evaluateVersusMinLLN(labValue: LabValue, minLLNFactor: Double): LabEvaluationResult {
        val refLimitLow = labValue.refLimitLow ?: return LabEvaluationResult.CANNOT_BE_DETERMINED
        val minValue = refLimitLow * minLLNFactor
        return evaluateVersusMinValueWithMargin(labValue.value, labValue.comparator, minValue)
    }

    fun evaluateVersusMaxULN(labValue: LabValue, maxULNFactor: Double): LabEvaluationResult {
        val refLimitUp = retrieveRefLimitUp(labValue) ?: return LabEvaluationResult.CANNOT_BE_DETERMINED
        val maxValue = refLimitUp * maxULNFactor
        return evaluateVersusMaxValueWithMargin(labValue.value, labValue.comparator, maxValue)
    }

    fun evaluateDifferenceVersusMaxULN(labValue1: LabValue, labValue2: LabValue, maxULNFactor: Double): LabEvaluationResult {
        val refLimitUp1 = retrieveRefLimitUp(labValue1) ?: return LabEvaluationResult.CANNOT_BE_DETERMINED
        val refLimitUp2 = retrieveRefLimitUp(labValue2) ?: return LabEvaluationResult.CANNOT_BE_DETERMINED
        val maxValue = (refLimitUp1 - refLimitUp2) * maxULNFactor
        return evaluateVersusMaxValueWithMargin(labValue1.value - labValue2.value, null, maxValue)
    }

    fun evaluateVersusMinValueWithMargin(
        value: Double, comparator: String?, minValue: Double
    ): LabEvaluationResult {
        return evaluateVersusValueWithMargin(value, comparator, minValue, true, LAB_VALUE_NEGATIVE_MARGIN_OF_ERROR)
    }

    fun evaluateVersusMaxValueWithMargin(
        value: Double, comparator: String?, maxValue: Double
    ): LabEvaluationResult {
        return evaluateVersusValueWithMargin(value, comparator, maxValue, false, LAB_VALUE_POSITIVE_MARGIN_OF_ERROR)
    }

    fun isValid(value: LabValue?, measurement: LabMeasurement, minValidDate: LocalDate): Boolean {
        return value != null && value.unit == measurement.defaultUnit && !value.date.isBefore(minValidDate)
    }

    fun evaluateInvalidLabValue(
        measurement: LabMeasurement,
        mostRecent: LabValue?,
        minValidDate: LocalDate
    ): Evaluation {
        return when {
            mostRecent == null -> {
                EvaluationFactory.recoverableUndetermined("No measurement found for ${measurement.display()}")
            }

            mostRecent.unit != measurement.defaultUnit -> {
                EvaluationFactory.recoverableUndetermined(
                    "Unexpected unit specified for ${measurement.display()}: ${mostRecent.unit.display()}"
                )
            }

            mostRecent.date.isBefore(minValidDate) -> {
                EvaluationFactory.recoverableUndetermined("Most recent measurement too old for ${measurement.display()}")
            }

            else -> {
                Evaluation(result = EvaluationResult.UNDETERMINED, recoverable = true)
            }
        }
    }

    private fun evaluateVersusValueWithMargin(
        value: Double, comparator: String?, threshold: Double, isMinValue: Boolean, margin: Double
    ): LabEvaluationResult {
        val thresholdWithMargin = threshold * margin
        return when {
            !canBeDetermined(value, comparator, threshold) -> LabEvaluationResult.CANNOT_BE_DETERMINED
            value == threshold || isMinValue == (value > threshold) -> LabEvaluationResult.WITHIN_THRESHOLD
            value == thresholdWithMargin || isMinValue == (value > thresholdWithMargin) -> LabEvaluationResult.EXCEEDS_THRESHOLD_BUT_WITHIN_MARGIN
            else -> LabEvaluationResult.EXCEEDS_THRESHOLD_AND_OUTSIDE_MARGIN
        }
    }

    private fun retrieveRefLimitUp(labValue: LabValue): Double? {
        return labValue.refLimitUp ?: REF_LIMIT_UP_OVERRIDES[labValue.measurement]
    }

    private fun canBeDetermined(value: Double, comparator: String?, refValue: Double): Boolean {
        return when (comparator) {
            ValueComparison.LARGER_THAN -> value > refValue
            ValueComparison.LARGER_THAN_OR_EQUAL -> value >= refValue
            ValueComparison.SMALLER_THAN -> value < refValue
            ValueComparison.SMALLER_THAN_OR_EQUAL -> value <= refValue
            else -> true
        }
    }

    internal enum class LabEvaluationResult {
        CANNOT_BE_DETERMINED,
        WITHIN_THRESHOLD,
        EXCEEDS_THRESHOLD_BUT_WITHIN_MARGIN,
        EXCEEDS_THRESHOLD_AND_OUTSIDE_MARGIN
    }
}