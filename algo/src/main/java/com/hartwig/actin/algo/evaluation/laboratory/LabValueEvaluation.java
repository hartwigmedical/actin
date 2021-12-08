package com.hartwig.actin.algo.evaluation.laboratory;

import com.google.common.annotations.VisibleForTesting;
import com.hartwig.actin.algo.datamodel.Evaluation;
import com.hartwig.actin.clinical.datamodel.LabValue;

import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

final class LabValueEvaluation {

    @VisibleForTesting
    static final String LARGER_THAN = ">";
    @VisibleForTesting
    static final String SMALLER_THAN = "<";

    private LabValueEvaluation() {
    }

    public static boolean existsWithExpectedUnit(@Nullable LabValue value, @NotNull String expectedUnit) {
        return value != null && value.unit().equals(expectedUnit);
    }

    @NotNull
    public static Evaluation evaluateVersusMinULN(@NotNull LabValue labValue, double minULN) {
        Double lowerLimit = labValue.refLimitLow();
        if (lowerLimit == null) {
            return Evaluation.UNDETERMINED;
        }

        double minValue = lowerLimit * minULN;
        return evaluateVersusMinValue(labValue.value(), labValue.comparator(), minValue);
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
    public static Evaluation evaluateVersusMaxULN(@NotNull LabValue labValue, double maxULN) {
        Double upperLimit = labValue.refLimitUp();
        if (upperLimit == null) {
            return Evaluation.UNDETERMINED;
        }

        double maxValue = upperLimit * maxULN;
        return evaluateVersusMaxValue(labValue.value(), labValue.comparator(), maxValue);
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
