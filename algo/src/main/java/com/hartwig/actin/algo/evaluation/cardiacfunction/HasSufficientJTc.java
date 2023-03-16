package com.hartwig.actin.algo.evaluation.cardiacfunction;

import java.util.Optional;

public class HasSufficientJTc extends EcgMeasureEvaluationFunction {

    public HasSufficientJTc(final double minJTC) {
        super("JTC",
                minJTC,
                ECGUnits.MILLISECONDS,
                ecg -> Optional.ofNullable(ecg.qtcfMeasure()),
                ThresholdCriteria.MINIMUM);
    }
}
