package com.hartwig.actin.algo.evaluation.laboratory

import com.hartwig.actin.algo.datamodel.EvaluationResult
import com.hartwig.actin.algo.evaluation.util.ValueComparison
import com.hartwig.actin.algo.evaluation.util.ValueComparison.evaluateVersusMaxValue
import com.hartwig.actin.algo.evaluation.util.ValueComparison.evaluateVersusMinValue
import com.hartwig.actin.clinical.datamodel.LabValue
import com.hartwig.actin.clinical.interpretation.LabMeasurement

internal object LabEvaluation {
    val REF_LIMIT_UP_OVERRIDES = mapOf(LabMeasurement.INTERNATIONAL_NORMALIZED_RATIO.code to 1.1)
    const val LAB_VALUE_NEGATIVE_MARGIN_OF_ERROR = 0.95
    const val LAB_VALUE_POSITIVE_MARGIN_OF_ERROR = 1.05

    fun evaluateVersusMinULN(labValue: LabValue, minULNFactor: Double, withMargin: Boolean): EvaluationResult {
        val refLimitUp = retrieveRefLimitUp(labValue) ?: return EvaluationResult.UNDETERMINED
        val minValue = refLimitUp * minULNFactor
        return if (withMargin) {
            evaluateVersusMinValueWithMargin(labValue.value, labValue.comparator, minValue)
        } else {
            evaluateVersusMinValue(labValue.value, labValue.comparator, minValue)
        }

    }

    fun evaluateVersusMinLLN(labValue: LabValue, minLLNFactor: Double, withMargin: Boolean): EvaluationResult {
        val refLimitLow = labValue.refLimitLow ?: return EvaluationResult.UNDETERMINED
        val minValue = refLimitLow * minLLNFactor
        return if (withMargin) {
            evaluateVersusMinValueWithMargin(labValue.value, labValue.comparator, minValue)
        } else {
            evaluateVersusMinValue(labValue.value, labValue.comparator, minValue)
        }
    }

    fun evaluateVersusMaxULN(labValue: LabValue, maxULNFactor: Double, withMargin: Boolean): EvaluationResult {
        val refLimitUp = retrieveRefLimitUp(labValue) ?: return EvaluationResult.UNDETERMINED
        val maxValue = refLimitUp * maxULNFactor
        return if (withMargin) {
            evaluateVersusMaxValueWithMargin(labValue.value, labValue.comparator, maxValue)
        } else {
            evaluateVersusMaxValue(labValue.value, labValue.comparator, maxValue)
        }
    }

    fun evaluateVersusMinValueWithMargin(
        value: Double, comparator: String?, minValue: Double
    ): EvaluationResult {
        return evaluateVersusValueWithMargin(value, comparator, minValue, true, LAB_VALUE_NEGATIVE_MARGIN_OF_ERROR)
    }

    fun evaluateVersusMaxValueWithMargin(
        value: Double, comparator: String?, maxValue: Double
    ): EvaluationResult {
        return evaluateVersusValueWithMargin(value, comparator, maxValue, false, LAB_VALUE_POSITIVE_MARGIN_OF_ERROR)
    }

    private fun evaluateVersusValueWithMargin(
        value: Double, comparator: String?, threshold: Double, isMinValue: Boolean, margin: Double
    ): EvaluationResult {
        if (!canBeDetermined(value, comparator, threshold)) {
            return EvaluationResult.UNDETERMINED
        }
        val thresholdWithMargin = threshold * margin

        return when {
            value == threshold || isMinValue == (value > threshold) -> EvaluationResult.PASS
            value == thresholdWithMargin || isMinValue == (value > thresholdWithMargin) -> EvaluationResult.WARN
            else -> EvaluationResult.FAIL
        }
    }

    private fun retrieveRefLimitUp(labValue: LabValue): Double? {
        return labValue.refLimitUp ?: REF_LIMIT_UP_OVERRIDES[labValue.code]
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
}