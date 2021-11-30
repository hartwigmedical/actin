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
    public static Evaluation evaluateOnMinimalValue(double value, @NotNull String comparator, double minValue) {
        if (comparator.equals(LARGER_THAN) && value < minValue || comparator.equals(SMALLER_THAN) && value > minValue) {
            return Evaluation.UNDETERMINED;
        } else {
            return Double.compare(value, minValue) >= 0 ? Evaluation.PASS : Evaluation.FAIL;
        }
    }
}
