package com.hartwig.actin.algo.evaluation.pregnancy;

import java.util.Map;

import com.google.common.collect.Maps;
import com.hartwig.actin.algo.evaluation.FunctionCreator;
import com.hartwig.actin.treatment.datamodel.EligibilityRule;

import org.jetbrains.annotations.NotNull;

public final class PregnancyRuleMapping {

    private PregnancyRuleMapping() {
    }

    @NotNull
    public static Map<EligibilityRule, FunctionCreator> create() {
        Map<EligibilityRule, FunctionCreator> map = Maps.newHashMap();

        map.put(EligibilityRule.IS_BREASTFEEDING, isBreastfeedingCreator());
        map.put(EligibilityRule.IS_PREGNANT, isPregnantCreator());
        map.put(EligibilityRule.IS_ABLE_AND_WILLING_TO_USE_ADEQUATE_ANTICONCEPTION, canUseAdequateAnticonceptionCreator());

        return map;
    }

    @NotNull
    private static FunctionCreator isBreastfeedingCreator() {
        return function -> new IsBreastfeeding();
    }

    @NotNull
    private static FunctionCreator isPregnantCreator() {
        return function -> new IsPregnant();
    }

    @NotNull
    private static FunctionCreator canUseAdequateAnticonceptionCreator() {
        return function -> new CanUseAdequateAnticonception();
    }
}
