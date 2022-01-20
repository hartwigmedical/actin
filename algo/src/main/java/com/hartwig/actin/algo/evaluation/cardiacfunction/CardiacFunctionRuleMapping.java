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
        map.put(EligibilityRule.HAS_CARDIAC_ARRHYTHMIA_OF_TYPE_X, function -> record -> Evaluation.NOT_IMPLEMENTED);
        map.put(EligibilityRule.HAS_LVEF_OF_AT_LEAST_X, hasSufficientLVEFCreator(false));
        map.put(EligibilityRule.HAS_LVEF_OF_AT_LEAST_X_IF_KNOWN, hasSufficientLVEFCreator(true));
        map.put(EligibilityRule.HAS_QTCF_OF_AT_MOST_X, function -> record -> Evaluation.NOT_IMPLEMENTED);
        map.put(EligibilityRule.HAS_LONG_QT_SYNDROME, function -> record -> Evaluation.NOT_IMPLEMENTED);
        map.put(EligibilityRule.HAS_RESTING_HEART_RATE_BETWEEN_X_AND_Y, function -> record -> Evaluation.NOT_IMPLEMENTED);

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
}
