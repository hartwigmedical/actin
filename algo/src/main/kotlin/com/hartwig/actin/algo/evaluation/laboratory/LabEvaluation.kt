package com.hartwig.actin.algo.evaluation.laboratory

import com.hartwig.actin.algo.datamodel.EvaluationResult
import com.hartwig.actin.algo.evaluation.util.ValueComparison.evaluateVersusMaxValue
import com.hartwig.actin.algo.evaluation.util.ValueComparison.evaluateVersusMinValue
import com.hartwig.actin.clinical.datamodel.LabValue
import com.hartwig.actin.clinical.interpretation.LabMeasurement

internal object LabEvaluation {
    val REF_LIMIT_UP_OVERRIDES = mapOf(LabMeasurement.INTERNATIONAL_NORMALIZED_RATIO.code to 1.1)
    const val LAB_VALUE_NEGATIVE_MARGIN_OF_ERROR = 0.9
    const val LAB_VALUE_POSITIVE_MARGIN_OF_ERROR = 1.1

    fun evaluateVersusMinULN(labValue: LabValue, minULNFactor: Double, withMargin: Boolean): EvaluationResult {
        val refLimitUp = retrieveRefLimitUp(labValue) ?: return EvaluationResult.UNDETERMINED
        val minValueWithMargin = refLimitUp * minULNFactor * LAB_VALUE_NEGATIVE_MARGIN_OF_ERROR
        val minValue = refLimitUp * minULNFactor
        return if (withMargin) {
            evaluateVersusMinValue(labValue.value, labValue.comparator, minValueWithMargin)
        } else {
            evaluateVersusMinValue(labValue.value, labValue.comparator, minValue)
        }

    }

    fun evaluateVersusMinLLN(labValue: LabValue, minLLNFactor: Double, withMargin: Boolean): EvaluationResult {
        val refLimitLow = labValue.refLimitLow ?: return EvaluationResult.UNDETERMINED
        val minValueWithMargin = refLimitLow * minLLNFactor * LAB_VALUE_NEGATIVE_MARGIN_OF_ERROR
        val minValue = refLimitLow * minLLNFactor
        return if (withMargin) {
            evaluateVersusMinValue(labValue.value, labValue.comparator, minValueWithMargin)
        } else {
            evaluateVersusMinValue(labValue.value, labValue.comparator, minValue)
        }
    }

    fun evaluateVersusMaxULN(labValue: LabValue, maxULNFactor: Double, withMargin: Boolean): EvaluationResult {
        val refLimitUp = retrieveRefLimitUp(labValue) ?: return EvaluationResult.UNDETERMINED
        val maxValue = refLimitUp * maxULNFactor
        val maxValueWithMargin = refLimitUp * maxULNFactor * LAB_VALUE_POSITIVE_MARGIN_OF_ERROR
        return if (withMargin) {
            evaluateVersusMaxValue(labValue.value, labValue.comparator, maxValueWithMargin)
        } else {
            evaluateVersusMaxValue(labValue.value, labValue.comparator, maxValue)
        }
    }

    private fun retrieveRefLimitUp(labValue: LabValue): Double? {
        return labValue.refLimitUp ?: REF_LIMIT_UP_OVERRIDES[labValue.code]
    }
}