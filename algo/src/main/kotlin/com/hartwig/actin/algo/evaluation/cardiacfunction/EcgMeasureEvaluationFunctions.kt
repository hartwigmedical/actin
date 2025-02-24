package com.hartwig.actin.algo.evaluation.cardiacfunction

import com.hartwig.actin.algo.evaluation.cardiacfunction.EcgMeasureEvaluationFunction.ThresholdCriteria
import com.hartwig.actin.datamodel.clinical.Ecg

object EcgMeasureEvaluationFunctions {

    fun hasLimitedQtcf(maxQtcf: Double): EcgMeasureEvaluationFunction {
        return EcgMeasureEvaluationFunction(
            EcgMeasureName.QTCF,
            maxQtcf,
            EcgUnit.MILLISECONDS,
            Ecg::qtcfMeasure,
            ThresholdCriteria.MAXIMUM
        )
    }

    fun hasSufficientQtcf(minQtcf: Double): EcgMeasureEvaluationFunction {
        return EcgMeasureEvaluationFunction(
            EcgMeasureName.QTCF,
            minQtcf,
            EcgUnit.MILLISECONDS,
            Ecg::qtcfMeasure,
            ThresholdCriteria.MINIMUM
        )
    }

    fun hasSufficientJTc(minJtc: Double): EcgMeasureEvaluationFunction {
        return EcgMeasureEvaluationFunction(
            EcgMeasureName.JTC,
            minJtc,
            EcgUnit.MILLISECONDS,
            Ecg::jtcMeasure,
            ThresholdCriteria.MINIMUM
        )
    }
}