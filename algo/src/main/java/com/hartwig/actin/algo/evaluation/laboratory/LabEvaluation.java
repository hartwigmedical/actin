package com.hartwig.actin.algo.evaluation.laboratory;

import java.util.Map;

import com.google.common.annotations.VisibleForTesting;
import com.google.common.collect.Maps;
import com.hartwig.actin.algo.datamodel.EvaluationResult;
import com.hartwig.actin.algo.evaluation.util.ValueComparison;
import com.hartwig.actin.clinical.datamodel.LabValue;
import com.hartwig.actin.clinical.interpretation.LabMeasurement;

import org.jetbrains.annotations.NotNull;

final class LabEvaluation {

    @VisibleForTesting
    static final Map<String, Double> REF_LIMIT_UP_OVERRIDES = Maps.newHashMap();

    static {
        REF_LIMIT_UP_OVERRIDES.put(LabMeasurement.INTERNATIONAL_NORMALIZED_RATIO.code(), 1.1);
    }

    private LabEvaluation() {
    }

    @NotNull
    public static EvaluationResult evaluateVersusMinULN(@NotNull LabValue labValue, double minULN) {
        Double refLimitLow = labValue.refLimitLow();
        if (refLimitLow == null) {
            return EvaluationResult.UNDETERMINED;
        }

        double minValue = refLimitLow * minULN;
        return ValueComparison.evaluateVersusMinValue(labValue.value(), labValue.comparator(), minValue);
    }

    @NotNull
    public static EvaluationResult evaluateVersusMaxULN(@NotNull LabValue labValue, double maxULN) {
        Double refLimitUp = labValue.refLimitUp();
        if (refLimitUp == null) {
            refLimitUp = REF_LIMIT_UP_OVERRIDES.get(labValue.code());
        }

        if (refLimitUp == null) {
            return EvaluationResult.UNDETERMINED;
        }

        double maxValue = refLimitUp * maxULN;
        return ValueComparison.evaluateVersusMaxValue(labValue.value(), labValue.comparator(), maxValue);
    }
}
