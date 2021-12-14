package com.hartwig.actin.algo.evaluation.cardiacfunction;

import java.util.Map;

import com.google.common.collect.Maps;
import com.hartwig.actin.algo.datamodel.Evaluation;
import com.hartwig.actin.algo.evaluation.FunctionCreator;
import com.hartwig.actin.treatment.datamodel.EligibilityRule;
import com.hartwig.actin.treatment.interpretation.FunctionInputResolver;

import org.jetbrains.annotations.NotNull;

public final class CardiacFunctionRuleMapping {

    private CardiacFunctionRuleMapping() {
    }

    @NotNull
    public static Map<EligibilityRule, FunctionCreator> create() {
        Map<EligibilityRule, FunctionCreator> map = Maps.newHashMap();

        map.put(EligibilityRule.HAS_CARDIAC_ARRHYTHMIA, hasCardiacArrhythmiaCreator());
        map.put(EligibilityRule.HAS_CARDIAC_ARRHYTHMIA_X, notImplementedCreator());
        map.put(EligibilityRule.HAS_LVEF_OF_AT_LEAST_X, hasSufficientLVEFCreator(false));
        map.put(EligibilityRule.HAS_LVEF_OF_AT_LEAST_X_IF_KNOWN, hasSufficientLVEFCreator(true));
        map.put(EligibilityRule.HAS_QTCF_OF_AT_MOST_X, hasLimitedQTCFCreator());
        map.put(EligibilityRule.HAS_LONG_QT_SYNDROME, hasLimitedQTCFCreator());
        map.put(EligibilityRule.HAS_RESTING_HEART_RATE_BETWEEN_X_AND_Y, hasLimitedQTCFCreator());

        return map;
    }

    @NotNull
    private static FunctionCreator hasCardiacArrhythmiaCreator() {
        return function -> new HasCardiacArrhythmia();
    }

    @NotNull
    private static FunctionCreator hasSufficientLVEFCreator(boolean passIfUnknown) {
        return function -> {
            double minLVEF = FunctionInputResolver.createOneDoubleInput(function);
            return new HasSufficientLVEF(minLVEF, passIfUnknown);
        };
    }

    @NotNull
    private static FunctionCreator hasLimitedQTCFCreator() {
        // TODO
        return function -> evaluation -> Evaluation.NOT_IMPLEMENTED;
    }

    @NotNull
    private static FunctionCreator notImplementedCreator() {
        return function -> evaluation -> Evaluation.NOT_IMPLEMENTED;
    }
}
