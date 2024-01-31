package com.hartwig.actin.algo.evaluation.laboratory

import com.hartwig.actin.algo.datamodel.EvaluationResult
import com.hartwig.actin.algo.evaluation.util.ValueComparison.evaluateVersusMaxValue
import com.hartwig.actin.algo.evaluation.util.ValueComparison.evaluateVersusMinValue
import com.hartwig.actin.clinical.datamodel.LabValue
import com.hartwig.actin.clinical.interpretation.LabMeasurement

internal object LabEvaluation {
    val REF_LIMIT_UP_OVERRIDES = mapOf(LabMeasurement.INTERNATIONAL_NORMALIZED_RATIO.code to 1.1)

    fun evaluateVersusMinULN(labValue: LabValue, minULNFactor: Double): EvaluationResult {
        val refLimitUp = retrieveRefLimitUp(labValue) ?: return EvaluationResult.UNDETERMINED
        val minValue = refLimitUp * minULNFactor
        return evaluateVersusMinValue(labValue.value, labValue.comparator, minValue)
    }

    fun evaluateVersusMinLLN(labValue: LabValue, minLLNFactor: Double): EvaluationResult {
        val refLimitLow = labValue.refLimitLow ?: return EvaluationResult.UNDETERMINED
        val minValue = refLimitLow * minLLNFactor
        return evaluateVersusMinValue(labValue.value, labValue.comparator, minValue)
    }

    fun evaluateVersusMaxULN(labValue: LabValue, maxULNFactor: Double): EvaluationResult {
        val refLimitUp = retrieveRefLimitUp(labValue) ?: return EvaluationResult.UNDETERMINED
        val maxValue = refLimitUp * maxULNFactor
        return evaluateVersusMaxValue(labValue.value, labValue.comparator, maxValue)
    }

    private fun retrieveRefLimitUp(labValue: LabValue): Double? {
        return labValue.refLimitUp ?: REF_LIMIT_UP_OVERRIDES[labValue.code]
    }
}