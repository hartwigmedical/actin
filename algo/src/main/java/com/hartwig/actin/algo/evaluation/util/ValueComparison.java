package com.hartwig.actin.algo.evaluation.util;

import java.util.Collection;

import com.hartwig.actin.algo.datamodel.EvaluationResult;

import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

public final class ValueComparison {

    public static final String LARGER_THAN = ">";
    public static final String LARGER_THAN_OR_EQUAL = ">=";
    public static final String SMALLER_THAN = "<";
    public static final String SMALLER_THAN_OR_EQUAL = "<=";

    private ValueComparison() {
    }

    @NotNull
    public static EvaluationResult evaluateVersusMinValue(double value, @Nullable String comparator, double minValue) {
        if (!canBeDetermined(value, comparator, minValue)) {
            return EvaluationResult.UNDETERMINED;
        }

        return Double.compare(value, minValue) >= 0 ? EvaluationResult.PASS : EvaluationResult.FAIL;
    }

    @NotNull
    public static EvaluationResult evaluateVersusMaxValue(double value, @Nullable String comparator, double maxValue) {
        if (!canBeDetermined(value, comparator, maxValue)) {
            return EvaluationResult.UNDETERMINED;
        }

        return Double.compare(value, maxValue) <= 0 ? EvaluationResult.PASS : EvaluationResult.FAIL;
    }

    private static boolean canBeDetermined(double value, @Nullable String comparator, double refValue) {
        if (comparator == null) {
            return true;
        }

         switch (comparator) {
            case LARGER_THAN: {
                return value > refValue;
            } case LARGER_THAN_OR_EQUAL: {
                return value >= refValue;
            } case SMALLER_THAN: {
                return value < refValue;
            } case SMALLER_THAN_OR_EQUAL: {
                return value <= refValue;
            }
            default: {
                return true;
            }
        }
    }

    public static boolean stringCaseInsensitivelyMatchesQueryCollection(String value, Collection<String> collection) {
        return collection.stream().anyMatch(termToFind -> value.toLowerCase().contains(termToFind.toLowerCase()));
    }
}
