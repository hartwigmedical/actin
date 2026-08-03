package com.hartwig.actin.algo.evaluation.cardiacfunction

import com.hartwig.actin.algo.evaluation.cardiacfunction.EcgMeasureEvaluationFunction.ThresholdCriteria
import com.hartwig.actin.datamodel.clinical.HeartMeasurementType

object EcgMeasureEvaluationFunctions {

    fun hasLimitedQtcf(maxQtcf: Double): EcgMeasureEvaluationFunction {
        return EcgMeasureEvaluationFunction(
            HeartMeasurementType.QTCF,
            maxQtcf,
            EcgUnit.MILLISECONDS,
            ThresholdCriteria.MAXIMUM
        )
    }

    fun hasSufficientQtcf(minQtcf: Double): EcgMeasureEvaluationFunction {
        return EcgMeasureEvaluationFunction(
            HeartMeasurementType.QTCF,
            minQtcf,
            EcgUnit.MILLISECONDS,
            ThresholdCriteria.MINIMUM
        )
    }

    fun hasSufficientJTc(minJtc: Double): EcgMeasureEvaluationFunction {
        return EcgMeasureEvaluationFunction(
            HeartMeasurementType.JTC,
            minJtc,
            EcgUnit.MILLISECONDS,
            ThresholdCriteria.MINIMUM
        )
    }
}