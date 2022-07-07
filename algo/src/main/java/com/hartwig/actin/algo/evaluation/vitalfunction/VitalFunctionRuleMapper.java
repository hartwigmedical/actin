package com.hartwig.actin.algo.evaluation.vitalfunction;

import java.util.Map;

import com.google.common.collect.Maps;
import com.hartwig.actin.algo.evaluation.FunctionCreator;
import com.hartwig.actin.algo.evaluation.RuleMapper;
import com.hartwig.actin.algo.evaluation.RuleMappingResources;
import com.hartwig.actin.treatment.datamodel.EligibilityRule;
import com.hartwig.actin.treatment.input.single.TwoDoubles;

import org.jetbrains.annotations.NotNull;

public class VitalFunctionRuleMapper extends RuleMapper {

    public VitalFunctionRuleMapper(@NotNull final RuleMappingResources resources) {
        super(resources);
    }

    @NotNull
    @Override
    public Map<EligibilityRule, FunctionCreator> createMappings() {
        Map<EligibilityRule, FunctionCreator> map = Maps.newHashMap();

        map.put(EligibilityRule.HAS_SBP_MMHG_OF_AT_LEAST_X, hasSufficientBloodPressureCreator(BloodPressureCategory.SYSTOLIC));
        map.put(EligibilityRule.HAS_SBP_MMHG_OF_AT_MOST_X, hasLimitedBloodPressureCreator(BloodPressureCategory.SYSTOLIC));
        map.put(EligibilityRule.HAS_DBP_MMHG_OF_AT_LEAST_X, hasSufficientBloodPressureCreator(BloodPressureCategory.DIASTOLIC));
        map.put(EligibilityRule.HAS_DBP_MMHG_OF_AT_MOST_X, hasLimitedBloodPressureCreator(BloodPressureCategory.DIASTOLIC));
        map.put(EligibilityRule.HAS_PULSE_OXIMETRY_OF_AT_LEAST_X, hasSufficientPulseOximetryCreator());
        map.put(EligibilityRule.HAS_RESTING_HEART_RATE_BETWEEN_X_AND_Y, hasRestingHeartRateWithinBoundsCreator());
        map.put(EligibilityRule.HAS_BODY_WEIGHT_OF_AT_LEAST_X, hasSufficientBodyWeightCreator());

        return map;
    }

    @NotNull
    private FunctionCreator hasSufficientBloodPressureCreator(@NotNull BloodPressureCategory category) {
        return function -> {
            double minMedianBloodPressure = functionInputResolver().createOneDoubleInput(function);
            return new HasSufficientBloodPressure(category, minMedianBloodPressure);
        };
    }

    @NotNull
    private FunctionCreator hasLimitedBloodPressureCreator(@NotNull BloodPressureCategory category) {
        return function -> {
            double maxMedianBloodPressure = functionInputResolver().createOneDoubleInput(function);
            return new HasLimitedBloodPressure(category, maxMedianBloodPressure);
        };
    }

    @NotNull
    private FunctionCreator hasSufficientPulseOximetryCreator() {
        return function -> {
            double minMedianPulseOximetry = functionInputResolver().createOneDoubleInput(function);
            return new HasSufficientPulseOximetry(minMedianPulseOximetry);
        };
    }

    @NotNull
    private FunctionCreator hasRestingHeartRateWithinBoundsCreator() {
        return function -> {
            TwoDoubles input = functionInputResolver().createTwoDoublesInput(function);
            return new HasRestingHeartRateWithinBounds(input.double1(), input.double2());
        };
    }

    @NotNull
    private FunctionCreator hasSufficientBodyWeightCreator() {
        return function -> {
            double minBodyWeight = functionInputResolver().createOneDoubleInput(function);
            return new HasSufficientBodyWeight(minBodyWeight);
        };
    }
}
