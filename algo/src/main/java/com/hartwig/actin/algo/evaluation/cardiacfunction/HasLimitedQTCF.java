package com.hartwig.actin.algo.evaluation.cardiacfunction;

import java.util.Optional;

public class HasLimitedQTCF extends ECGMeasureEvaluationFunction {

    public HasLimitedQTCF(final double maxQTCF) {
        super(ECGMeasures.QTCF,
                maxQTCF,
                ECGUnits.MILLISECONDS,
                ecg -> Optional.ofNullable(ecg.qtcfMeasure()),
                ThresholdCriteria.MAXIMUM);
    }
}
