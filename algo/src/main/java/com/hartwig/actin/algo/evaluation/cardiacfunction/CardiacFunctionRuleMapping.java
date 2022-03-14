package com.hartwig.actin.algo.evaluation.cardiacfunction;

import java.util.Map;

import com.google.common.collect.Maps;
import com.hartwig.actin.algo.doid.DoidModel;
import com.hartwig.actin.algo.evaluation.FunctionCreator;
import com.hartwig.actin.treatment.datamodel.EligibilityRule;
import com.hartwig.actin.treatment.input.FunctionInputResolver;

import org.jetbrains.annotations.NotNull;

public final class CardiacFunctionRuleMapping {

    private CardiacFunctionRuleMapping() {
    }

    @NotNull
    public static Map<EligibilityRule, FunctionCreator> create(@NotNull DoidModel doidModel) {
        Map<EligibilityRule, FunctionCreator> map = Maps.newHashMap();

        map.put(EligibilityRule.HAS_CARDIAC_ARRHYTHMIA, hasAnyTypeOfCardiacArrhythmiaCreator());
        map.put(EligibilityRule.HAS_CARDIAC_ARRHYTHMIA_OF_TYPE_X, hasCardiacArrhythmiaOfSpecificTypeCreator());
        map.put(EligibilityRule.HAS_LVEF_OF_AT_LEAST_X, hasSufficientLVEFCreator(false));
        map.put(EligibilityRule.HAS_LVEF_OF_AT_LEAST_X_IF_KNOWN, hasSufficientLVEFCreator(true));
        map.put(EligibilityRule.HAS_QTC_OF_AT_MOST_X, hasLimitedQTCFCreator());
        map.put(EligibilityRule.HAS_QTCF_OF_AT_MOST_X, hasLimitedQTCFCreator());
        map.put(EligibilityRule.HAS_LONG_QT_SYNDROME, hasLongQTSyndromeCreator(doidModel));
        map.put(EligibilityRule.HAS_RESTING_HEART_RATE_BETWEEN_X_AND_Y, hasRestingHeartRateWithinBoundsCreator());

        return map;
    }

    @NotNull
    private static FunctionCreator hasAnyTypeOfCardiacArrhythmiaCreator() {
        return function -> new HasCardiacArrhythmia(null);
    }

    @NotNull
    private static FunctionCreator hasCardiacArrhythmiaOfSpecificTypeCreator() {
        return function -> {
            String type = FunctionInputResolver.createOneStringInput(function);
            return new HasCardiacArrhythmia(type);
        };
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
        return function -> {
            double maxQTCF = FunctionInputResolver.createOneDoubleInput(function);
            return new HasLimitedQTCF(maxQTCF);
        };
    }

    @NotNull
    private static FunctionCreator hasLongQTSyndromeCreator(@NotNull DoidModel doidModel) {
        return function -> new HasLongQTSyndrome(doidModel);
    }

    @NotNull
    private static FunctionCreator hasRestingHeartRateWithinBoundsCreator() {
        return function -> new HasRestingHeartRateWithinBounds();
    }
}
