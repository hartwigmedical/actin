package com.hartwig.actin.algo.evaluation.cardiacfunction

import com.hartwig.actin.algo.evaluation.EvaluationLabels
import com.hartwig.actin.algo.evaluation.cardiacfunction.EcgMeasureEvaluationFunction.ThresholdCriteria
import com.hartwig.actin.datamodel.clinical.Ecg

object EcgMeasureEvaluationFunctions {

    fun hasLimitedQtcf(maxQtcf: Double, labels: EvaluationLabels.CardiacFunction): EcgMeasureEvaluationFunction {
        return EcgMeasureEvaluationFunction(
            EcgMeasureName.QTCF,
            maxQtcf,
            EcgUnit.MILLISECONDS,
            Ecg::qtcfMeasure,
            ThresholdCriteria.MAXIMUM,
            labels
        )
    }

    fun hasSufficientQtcf(minQtcf: Double, labels: EvaluationLabels.CardiacFunction): EcgMeasureEvaluationFunction {
        return EcgMeasureEvaluationFunction(
            EcgMeasureName.QTCF,
            minQtcf,
            EcgUnit.MILLISECONDS,
            Ecg::qtcfMeasure,
            ThresholdCriteria.MINIMUM,
            labels
        )
    }

    fun hasSufficientJTc(minJtc: Double, labels: EvaluationLabels.CardiacFunction): EcgMeasureEvaluationFunction {
        return EcgMeasureEvaluationFunction(
            EcgMeasureName.JTC,
            minJtc,
            EcgUnit.MILLISECONDS,
            Ecg::jtcMeasure,
            ThresholdCriteria.MINIMUM,
            labels
        )
    }
}