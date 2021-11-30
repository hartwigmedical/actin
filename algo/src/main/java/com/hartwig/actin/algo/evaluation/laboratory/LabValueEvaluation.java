package com.hartwig.actin.algo.evaluation.laboratory;

import com.google.common.annotations.VisibleForTesting;
import com.hartwig.actin.algo.datamodel.Evaluation;

import org.jetbrains.annotations.NotNull;

final class LabValueEvaluation {

    @VisibleForTesting
    static final String LARGER_THAN = ">";
    @VisibleForTesting
    static final String SMALLER_THAN = "<";

    private LabValueEvaluation() {
    }

    @NotNull
    public static Evaluation evaluateVersusMinValue(double value, @NotNull String comparator, double minValue) {
        if (cannotBeDetermined(value, comparator, minValue)) {
            return Evaluation.UNDETERMINED;
        } else {
            return Double.compare(value, minValue) >= 0 ? Evaluation.PASS : Evaluation.FAIL;
        }
    }

    @NotNull
    public static Evaluation evaluateVersusMaxValue(double value, @NotNull String comparator, double maxValue) {
        if (cannotBeDetermined(value, comparator, maxValue)) {
            return Evaluation.UNDETERMINED;
        } else {
            return Double.compare(value, maxValue) <= 0 ? Evaluation.PASS : Evaluation.FAIL;
        }
    }

    private static boolean cannotBeDetermined(double value, @NotNull String comparator, double refValue) {
        return comparator.equals(LARGER_THAN) && value < refValue || comparator.equals(SMALLER_THAN) && value > refValue;
    }
}
