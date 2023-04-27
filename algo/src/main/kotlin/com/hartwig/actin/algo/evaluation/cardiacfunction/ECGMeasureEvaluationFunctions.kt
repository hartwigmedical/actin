package com.hartwig.actin.algo.evaluation.cardiacfunction;

import java.util.Optional;

public class ECGMeasureEvaluationFunctions {

    private ECGMeasureEvaluationFunctions() {
    }

    static ECGMeasureEvaluationFunction hasLimitedQTCF(final double maxQTCF) {
        return new ECGMeasureEvaluationFunction(ECGMeasureName.QTCF,
                maxQTCF,
                ECGUnit.MILLISECONDS,
                ecg -> Optional.ofNullable(ecg.qtcfMeasure()),
                ECGMeasureEvaluationFunction.ThresholdCriteria.MAXIMUM);
    }

    static ECGMeasureEvaluationFunction hasSufficientQTCF(final double minQTCF) {
        return new ECGMeasureEvaluationFunction(ECGMeasureName.QTCF,
                minQTCF,
                ECGUnit.MILLISECONDS,
                ecg -> Optional.ofNullable(ecg.qtcfMeasure()),
                ECGMeasureEvaluationFunction.ThresholdCriteria.MINIMUM);
    }

    static ECGMeasureEvaluationFunction hasSufficientJTc(final double maxQTCF) {
        return new ECGMeasureEvaluationFunction(ECGMeasureName.JTC,
                maxQTCF,
                ECGUnit.MILLISECONDS,
                ecg -> Optional.ofNullable(ecg.jtcMeasure()),
                ECGMeasureEvaluationFunction.ThresholdCriteria.MINIMUM);
    }
}
