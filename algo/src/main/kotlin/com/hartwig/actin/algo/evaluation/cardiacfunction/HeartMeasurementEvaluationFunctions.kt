package com.hartwig.actin.algo.evaluation.cardiacfunction

import com.hartwig.actin.algo.evaluation.cardiacfunction.HeartMeasurementEvaluationFunction.ThresholdCriteria
import com.hartwig.actin.datamodel.clinical.HeartMeasurementType

object HeartMeasurementEvaluationFunctions {

    fun hasLimitedQtcf(maxQtcf: Double): HeartMeasurementEvaluationFunction {
        return HeartMeasurementEvaluationFunction(
            HeartMeasurementType.QTCF,
            maxQtcf,
            EcgUnit.MILLISECONDS,
            ThresholdCriteria.MAXIMUM
        )
    }

    fun hasSufficientQtcf(minQtcf: Double): HeartMeasurementEvaluationFunction {
        return HeartMeasurementEvaluationFunction(
            HeartMeasurementType.QTCF,
            minQtcf,
            EcgUnit.MILLISECONDS,
            ThresholdCriteria.MINIMUM
        )
    }

    fun hasSufficientJTc(minJtc: Double): HeartMeasurementEvaluationFunction {
        return HeartMeasurementEvaluationFunction(
            HeartMeasurementType.JTC,
            minJtc,
            EcgUnit.MILLISECONDS,
            ThresholdCriteria.MINIMUM
        )
    }
}