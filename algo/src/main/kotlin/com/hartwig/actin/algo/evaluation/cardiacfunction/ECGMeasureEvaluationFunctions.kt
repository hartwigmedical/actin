package com.hartwig.actin.algo.evaluation.cardiacfunction

import com.hartwig.actin.algo.evaluation.cardiacfunction.ECGMeasureEvaluationFunction.ThresholdCriteria
import com.hartwig.actin.datamodel.clinical.ECG

object ECGMeasureEvaluationFunctions {
    fun hasLimitedQTCF(maxQTCF: Double): ECGMeasureEvaluationFunction {
        return ECGMeasureEvaluationFunction(
            ECGMeasureName.QTCF,
            maxQTCF,
            ECGUnit.MILLISECONDS,
            ECG::qtcfMeasure,
            ThresholdCriteria.MAXIMUM
        )
    }

    fun hasSufficientQTCF(minQTCF: Double): ECGMeasureEvaluationFunction {
        return ECGMeasureEvaluationFunction(
            ECGMeasureName.QTCF,
            minQTCF,
            ECGUnit.MILLISECONDS,
            ECG::qtcfMeasure,
            ThresholdCriteria.MINIMUM
        )
    }

    fun hasSufficientJTc(minJTC: Double): ECGMeasureEvaluationFunction {
        return ECGMeasureEvaluationFunction(
            ECGMeasureName.JTC,
            minJTC,
            ECGUnit.MILLISECONDS,
            ECG::jtcMeasure,
            ThresholdCriteria.MINIMUM
        )
    }
}