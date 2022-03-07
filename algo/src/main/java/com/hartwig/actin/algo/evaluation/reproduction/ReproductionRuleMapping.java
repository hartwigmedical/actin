package com.hartwig.actin.algo.evaluation.reproduction;

import java.util.Map;

import com.google.common.collect.Maps;
import com.hartwig.actin.algo.evaluation.FunctionCreator;
import com.hartwig.actin.treatment.datamodel.EligibilityRule;

import org.jetbrains.annotations.NotNull;

public final class ReproductionRuleMapping {

    private ReproductionRuleMapping() {
    }

    @NotNull
    public static Map<EligibilityRule, FunctionCreator> create() {
        Map<EligibilityRule, FunctionCreator> map = Maps.newHashMap();

        map.put(EligibilityRule.IS_BREASTFEEDING, isBreastfeedingCreator());
        map.put(EligibilityRule.IS_PREGNANT, isPregnantCreator());
        map.put(EligibilityRule.USES_ADEQUATE_ANTICONCEPTION, canUseAdequateAnticonceptionCreator());
        map.put(EligibilityRule.ADHERES_TO_SPERM_OR_EGG_DONATION_PRESCRIPTIONS, willingToAdhereToDonationPrescriptionsCreator());

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

    @NotNull
    private static FunctionCreator willingToAdhereToDonationPrescriptionsCreator() {
        return function -> new WillingToAdhereToDonationPrescriptions();
    }
}
