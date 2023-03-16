package com.hartwig.actin.algo.evaluation.cardiacfunction;

import java.util.Optional;

public class HasLimitedQTCF extends EcgMeasureEvaluationFunction {

    public HasLimitedQTCF(final double maxQTCF) {
        super("QTCF",
                maxQTCF,
                ECGUnits.MILLISECONDS,
                ecg -> Optional.ofNullable(ecg.qtcfMeasure()),
                ThresholdCriteria.MAXIMUM);
    }
}
