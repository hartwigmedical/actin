package com.hartwig.actin.algo.evaluation.cardiacfunction;

import java.util.Optional;

public class HasSufficientQTCF extends ECGMeasureEvaluationFunction {

    public HasSufficientQTCF(final double minQTCF) {
        super(ECGMeasures.QTCF,
                minQTCF,
                ECGUnits.MILLISECONDS,
                ecg -> Optional.ofNullable(ecg.qtcfMeasure()),
                ThresholdCriteria.MINIMUM);
    }
}