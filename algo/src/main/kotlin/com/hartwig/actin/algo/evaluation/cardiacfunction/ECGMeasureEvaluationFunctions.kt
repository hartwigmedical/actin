package com.hartwig.actin.algo.evaluation.cardiacfunction

import com.hartwig.actin.algo.evaluation.cardiacfunction.ECGMeasureEvaluationFunction.ThresholdCriteria
import com.hartwig.actin.clinical.datamodel.ECG

object ECGMeasureEvaluationFunctions {
    fun hasLimitedQTCF(maxQTCF: Double): ECGMeasureEvaluationFunction {
        return ECGMeasureEvaluationFunction(
            ECGMeasureName.QTCF,
            maxQTCF,
            ECGUnit.MILLISECONDS,
            { ecg: ECG -> ecg.qtcfMeasure() },
            ThresholdCriteria.MAXIMUM
        )
    }

    fun hasSufficientQTCF(minQTCF: Double): ECGMeasureEvaluationFunction {
        return ECGMeasureEvaluationFunction(
            ECGMeasureName.QTCF,
            minQTCF,
            ECGUnit.MILLISECONDS,
            { ecg: ECG -> ecg.qtcfMeasure() },
            ThresholdCriteria.MINIMUM
        )
    }

    fun hasSufficientJTc(minJTC: Double): ECGMeasureEvaluationFunction {
        return ECGMeasureEvaluationFunction(
            ECGMeasureName.JTC,
            minJTC,
            ECGUnit.MILLISECONDS,
            { ecg: ECG -> ecg.jtcMeasure() },
            ThresholdCriteria.MINIMUM
        )
    }
}