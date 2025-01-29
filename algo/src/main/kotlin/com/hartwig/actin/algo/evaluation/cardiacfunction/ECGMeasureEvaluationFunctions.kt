package com.hartwig.actin.algo.evaluation.cardiacfunction

import com.hartwig.actin.algo.evaluation.cardiacfunction.ECGMeasureEvaluationFunction.ThresholdCriteria
import com.hartwig.actin.datamodel.clinical.Ecg

object ECGMeasureEvaluationFunctions {

    fun hasLimitedQTCF(maxQTCF: Double): ECGMeasureEvaluationFunction {
        return ECGMeasureEvaluationFunction(
            ECGMeasureName.QTCF,
            maxQTCF,
            ECGUnit.MILLISECONDS,
            Ecg::qtcfMeasure,
            ThresholdCriteria.MAXIMUM
        )
    }

    fun hasSufficientQTCF(minQTCF: Double): ECGMeasureEvaluationFunction {
        return ECGMeasureEvaluationFunction(
            ECGMeasureName.QTCF,
            minQTCF,
            ECGUnit.MILLISECONDS,
            Ecg::qtcfMeasure,
            ThresholdCriteria.MINIMUM
        )
    }

    fun hasSufficientJTc(minJTC: Double): ECGMeasureEvaluationFunction {
        return ECGMeasureEvaluationFunction(
            ECGMeasureName.JTC,
            minJTC,
            ECGUnit.MILLISECONDS,
            Ecg::jtcMeasure,
            ThresholdCriteria.MINIMUM
        )
    }
}