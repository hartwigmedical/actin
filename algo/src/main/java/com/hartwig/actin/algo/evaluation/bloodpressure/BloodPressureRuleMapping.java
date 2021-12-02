package com.hartwig.actin.algo.evaluation.bloodpressure;

import java.util.Map;

import com.google.common.collect.Maps;
import com.hartwig.actin.algo.evaluation.FunctionCreator;
import com.hartwig.actin.treatment.datamodel.EligibilityRule;

import org.jetbrains.annotations.NotNull;

public final class BloodPressureRuleMapping {

    private BloodPressureRuleMapping() {
    }

    @NotNull
    public static Map<EligibilityRule, FunctionCreator> create() {
        Map<EligibilityRule, FunctionCreator> map = Maps.newHashMap();

        map.put(EligibilityRule.HAS_SBP_MMHG_OF_AT_MOST_X, hasLimitedSBPCreator());
        map.put(EligibilityRule.HAS_DBP_MMHG_OF_AT_MOST_X, hasLimitedDBPCreator());

        return map;
    }

    @NotNull
    private static FunctionCreator hasLimitedDBPCreator() {
        return function -> new HasLimitedDBP();
    }

    @NotNull
    private static FunctionCreator hasLimitedSBPCreator() {
        return function -> new HasLimitedSBP();
    }
}
