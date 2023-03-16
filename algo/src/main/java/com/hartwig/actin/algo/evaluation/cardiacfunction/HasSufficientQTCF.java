package com.hartwig.actin.algo.evaluation.cardiacfunction;

import java.util.Optional;

public class HasSufficientQTCF extends EcgMeasureEvaluationFunction {

    public HasSufficientQTCF(final double minQTCF) {
        super("QTCF",
                minQTCF,
                ECGUnits.MILLISECONDS,
                ecg -> Optional.ofNullable(ecg.qtcfMeasure()),
                ThresholdCriteria.MINIMUM);
    }
}