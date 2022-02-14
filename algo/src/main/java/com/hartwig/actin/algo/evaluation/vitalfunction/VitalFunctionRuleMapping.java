package com.hartwig.actin.algo.evaluation.vitalfunction;

import java.util.Map;

import com.google.common.collect.Maps;
import com.hartwig.actin.algo.datamodel.Evaluation;
import com.hartwig.actin.algo.evaluation.FunctionCreator;
import com.hartwig.actin.treatment.datamodel.EligibilityRule;
import com.hartwig.actin.treatment.interpretation.FunctionInputResolver;

import org.jetbrains.annotations.NotNull;

public final class VitalFunctionRuleMapping {

    private VitalFunctionRuleMapping() {
    }

    @NotNull
    public static Map<EligibilityRule, FunctionCreator> create() {
        Map<EligibilityRule, FunctionCreator> map = Maps.newHashMap();

        map.put(EligibilityRule.HAS_SBP_MMHG_OF_AT_LEAST_X, hasSufficientBloodPressureCreator(BloodPressureCategory.SYSTOLIC));
        map.put(EligibilityRule.HAS_DBP_MMHG_OF_AT_LEAST_X, hasSufficientBloodPressureCreator(BloodPressureCategory.DIASTOLIC));
        map.put(EligibilityRule.HAS_PULSE_OXYMETRY_OF_AT_LEAST_X, function -> record -> Evaluation.NOT_IMPLEMENTED);
        map.put(EligibilityRule.HAS_BODY_WEIGHT_OF_AT_LEAST_X, function -> record -> Evaluation.NOT_IMPLEMENTED);

        return map;
    }

    @NotNull
    private static FunctionCreator hasSufficientBloodPressureCreator(@NotNull BloodPressureCategory category) {
        return function -> {
            double minAvgBloodPressure = FunctionInputResolver.createOneDoubleInput(function);
            return new HasSufficientBloodPressure(category, minAvgBloodPressure);
        };
    }
}
