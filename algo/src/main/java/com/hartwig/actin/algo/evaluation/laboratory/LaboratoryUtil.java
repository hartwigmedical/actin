package com.hartwig.actin.algo.evaluation.laboratory;

import java.util.Map;

import com.google.common.annotations.VisibleForTesting;
import com.google.common.collect.Maps;
import com.hartwig.actin.algo.datamodel.Evaluation;
import com.hartwig.actin.algo.datamodel.EvaluationResult;
import com.hartwig.actin.algo.datamodel.ImmutableEvaluation;
import com.hartwig.actin.algo.evaluation.EvaluationFactory;
import com.hartwig.actin.clinical.datamodel.LabValue;
import com.hartwig.actin.clinical.interpretation.LabMeasurement;

import org.jetbrains.annotations.NotNull;

final class LaboratoryUtil {

    @VisibleForTesting
    static final String LARGER_THAN = ">";
    @VisibleForTesting
    static final String SMALLER_THAN = "<";

    @VisibleForTesting
    static final Map<String, Double> REF_LIMIT_UP_OVERRIDES = Maps.newHashMap();

    static {
        REF_LIMIT_UP_OVERRIDES.put(LabMeasurement.INTERNATIONAL_NORMALIZED_RATIO.code(), 1.1);
    }

    private LaboratoryUtil() {
    }

    @NotNull
    public static Evaluation evaluateVersusMinULN(@NotNull LabValue labValue, double minULN) {
        Double refLimitLow = labValue.refLimitLow();
        if (refLimitLow == null) {
            return EvaluationFactory.create(EvaluationResult.UNDETERMINED);
        }

        double minValue = refLimitLow * minULN;
        return evaluateVersusMinValue(labValue.code(), labValue.value(), labValue.comparator(), minValue);
    }

    @NotNull
    public static Evaluation evaluateVersusMinValue(@NotNull String labCode, double value, @NotNull String comparator, double minValue) {
        if (cannotBeDetermined(value, comparator, minValue)) {
            return ImmutableEvaluation.builder()
                    .result(EvaluationResult.UNDETERMINED)
                    .addUndeterminedMessages(labCode + " cannot be determined with available data")
                    .build();
        }

        EvaluationResult result = Double.compare(value, minValue) >= 0 ? EvaluationResult.PASS : EvaluationResult.FAIL;
        ImmutableEvaluation.Builder builder = ImmutableEvaluation.builder().result(result);
        if (result == EvaluationResult.FAIL) {
            builder.addFailMessages(labCode + " " + value + " is not acceptable");
        } else if (result == EvaluationResult.PASS) {
            builder.addPassMessages(labCode + " " + value + " is acceptable");
        }
        return builder.build();
    }

    @NotNull
    public static Evaluation evaluateVersusMaxULN(@NotNull LabValue labValue, double maxULN) {
        Double refLimitUp = labValue.refLimitUp();
        if (refLimitUp == null) {
            refLimitUp = REF_LIMIT_UP_OVERRIDES.get(labValue.code());
        }

        if (refLimitUp == null) {
            return EvaluationFactory.create(EvaluationResult.UNDETERMINED);
        }

        double maxValue = refLimitUp * maxULN;
        return evaluateVersusMaxValue(labValue.value(), labValue.comparator(), maxValue);
    }

    @NotNull
    public static Evaluation evaluateVersusMaxValue(double value, @NotNull String comparator, double maxValue) {
        if (cannotBeDetermined(value, comparator, maxValue)) {
            return EvaluationFactory.create(EvaluationResult.UNDETERMINED);
        } else {
            EvaluationResult result = Double.compare(value, maxValue) <= 0 ? EvaluationResult.PASS : EvaluationResult.FAIL;
            return EvaluationFactory.create(result);
        }
    }

    private static boolean cannotBeDetermined(double value, @NotNull String comparator, double refValue) {
        return comparator.equals(LARGER_THAN) && value < refValue || comparator.equals(SMALLER_THAN) && value > refValue;
    }
}
