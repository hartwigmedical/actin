package com.hartwig.actin.algo.evaluation.cardiacfunction;

import java.util.Map;

import com.google.common.collect.Maps;
import com.hartwig.actin.algo.datamodel.Evaluation;
import com.hartwig.actin.algo.evaluation.FunctionCreator;
import com.hartwig.actin.treatment.datamodel.EligibilityRule;

import org.jetbrains.annotations.NotNull;

public final class CardiacFunctionRuleMapping {

    private CardiacFunctionRuleMapping() {
    }

    @NotNull
    public static Map<EligibilityRule, FunctionCreator> create() {
        Map<EligibilityRule, FunctionCreator> map = Maps.newHashMap();

        map.put(EligibilityRule.HAS_QTCF_OF_AT_MOST_X, hasLimitedQTCFCreator());
        map.put(EligibilityRule.HAS_LONG_QT_SYNDROME, hasLimitedQTCFCreator());
        map.put(EligibilityRule.HAS_RESTING_HEART_RATE_BETWEEN_X_AND_Y, hasLimitedQTCFCreator());

        return map;
    }

    @NotNull
    private static FunctionCreator hasLimitedQTCFCreator() {
        // TODO
        return function -> evaluation -> Evaluation.NOT_IMPLEMENTED;
    }
}
